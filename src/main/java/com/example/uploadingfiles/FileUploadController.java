package com.example.uploadingfiles;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.uploadingfiles.storage.StorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class FileUploadController {
    private final ConfigurableApplicationContext context;
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);
      public FileUploadController( ConfigurableApplicationContext context) {
      
        this.context = context;

    }
    @GetMapping("/")
    public String listUploadedFiles() {

        return "index";

    }
      @PostMapping("/restartApplication")
    public ResponseEntity<String> restartApplication() {
        if (!isRestarting.compareAndSet(false, true)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Restart is already in progress");
        }

        try {
            log.info("Initiating application restart with cleanup...");
            
            // 1. Cancel all scheduled tasks
            cancelAllScheduledTasks();
            
            // 2. Shutdown async tasks and thread pools
            shutdownAsyncExecutors();
            
            // 3. Clear caches and reset services
            resetApplicationServices();
            
            // 4. Close the context
            if (context.isActive()) {
                log.info("Closing Spring context...");
                context.close();
                log.info("Context closed successfully");
            }

            // 5. Force garbage collection
            System.gc();
            System.runFinalization();
            log.info("Garbage collection completed");
            
            // 6. Wait for cleanup
            TimeUnit.SECONDS.sleep(5);
            
            // 7. Start new instance
            new Thread(() -> {
                try {
                    SpringApplication.run(StickersCreateApplication.class);
                } catch (Exception e) {
                    log.error("Failed to restart application", e);
                }
            }).start();

            return ResponseEntity.ok("Restart initiated with full cleanup");
        } catch (Exception e) {
            log.error("Critical error during restart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during restart: " + e.getMessage());
        } finally {
            isRestarting.set(false);
        }
    }

    private void cancelAllScheduledTasks() {
        try {
            ScheduledExecutorService scheduledExecutor = context.getBean(ScheduledExecutorService.class);
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                log.info("Scheduled tasks shutdown initiated");
            }
        } catch (Exception e) {
            log.warn("Error shutting down scheduled tasks", e);
        }
    }

    private void shutdownAsyncExecutors() {
        try {
            // Get all thread pool executors
            Map<String, ExecutorService> executors = context.getBeansOfType(ExecutorService.class);
            executors.forEach((name, executor) -> {
                try {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                        log.warn("Executor {} did not terminate gracefully", name);
                    }
                } catch (Exception e) {
                    log.warn("Error shutting down executor {}", name, e);
                }
            });
            
            // Handle ForkJoinPool specifically
            ForkJoinPool.commonPool().shutdownNow();
            log.info("Async executors shutdown completed");
        } catch (Exception e) {
            log.warn("Error shutting down async executors", e);
        }
    }

    private void resetApplicationServices() {
 
            
           
            // Clear any caches
            try {
                CacheManager cacheManager = context.getBean(CacheManager.class);
                if (cacheManager != null) {
                    cacheManager.getCacheNames().forEach(name -> {
                        Cache cache = cacheManager.getCache(name);
                        if (cache != null) {
                            cache.clear();
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Could not clear caches", e);
            }
            
            log.info("Application services reset completed");
       
    }


}


