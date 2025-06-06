<template>
  <div>
    <div class="header text-left sticky-top">
      <table style="width: 100%;">
        <tbody>
          <tr style="display: flex; justify-content: space-between;">
            <td>
              <h3>Сервис формирования стикеров на основании XLSx файлов</h3>
            </td>
            <td>
              <button 
                @click="restartApplication"
                class="btn btn-outline-danger me-2"
                :class="{ 'btn-danger': isRestarting }"
                :disabled="isRestarting"
              >
                <span v-if="!isRestarting">
                  <i class="bi bi-arrow-clockwise me-1"></i>  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M2 12C2 7.28595 2 4.92893 3.46447 3.46447C4.92893 2 7.28595 2 12 2C16.714 2 19.0711 2 20.5355 3.46447C22 4.92893 22 7.28595 22 12C22 16.714 22 19.0711 20.5355 20.5355C19.0711 22 16.714 22 12 22C7.28595 22 4.92893 22 3.46447 20.5355C2 19.0711 2 16.714 2 12ZM15.9346 5.59158C16.217 5.70662 16.4017 5.98121 16.4017 6.28616V9.00067C16.4017 9.41489 16.0659 9.75067 15.6517 9.75067H13C12.6983 9.75067 12.4259 9.56984 12.3088 9.29174C12.1917 9.01364 12.2527 8.69245 12.4635 8.47659L13.225 7.69705C11.7795 7.25143 10.1467 7.61303 9.00097 8.78596C7.33301 10.4935 7.33301 13.269 9.00097 14.9765C10.6593 16.6742 13.3407 16.6742 14.999 14.9765C15.6769 14.2826 16.0805 13.4112 16.2069 12.5045C16.2651 12.0865 16.5972 11.7349 17.0192 11.7349C17.4246 11.7349 17.7609 12.0595 17.7217 12.463C17.5957 13.7606 17.0471 15.0265 16.072 16.0247C13.8252 18.3248 10.1748 18.3248 7.92796 16.0247C5.69068 13.7344 5.69068 10.0281 7.92796 7.7378C9.66551 5.95905 12.244 5.55465 14.3647 6.53037L15.1152 5.76208C15.3283 5.54393 15.6522 5.47653 15.9346 5.59158Z" fill="red"/>
              </svg>
                </span>
                <span v-else>
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Перезапуск...
                </span>
              </button>
             
            </td>
          </tr>
        </tbody>
      </table>

      <div>
        <span>Краткая инструкция:</span> <br>
        <span>1. Убедиться, что номенклатура загружена</span><br>
        <span>2. Загрузить файл заказа. После выбора файла обработка начнется автоматически.</span><br>
        <span>3. Посмотреть/распечатать файл из списка </span><br>
      </div>

      <div>
        <form @submit.prevent="handleFileUpload" enctype="multipart/form-data">
          <div class="form-row">
            <div class="form-cell file-upload-container">
              <span>Выберите файл для загрузки:</span>
              <label class="btn btn-primary custom-file-upload">
                <input type="file" name="file" accept=".xlsx" @change="onFileChange" /> Выбрать файл
              </label>
              <span v-if="selectedFile" class="file-name">
                {{ selectedFile.name }}
                <span v-if="isLoading" class="pulsating-dots">
                  <span class="dot">.</span>
                  <span class="dot">.</span>
                  <span class="dot">.</span>
                </span>
              </span>
              <span v-else class="file-name">Файл не выбран</span>
            </div>
            <div class="form-cell-right">
              <button 
                type="submit" 
                class="btn btn-primary" 
                :disabled="isLoading"
                :class="{
                  'btn-hover': !isLoading,
                  'btn-active': isButtonActive
                }"
                @mousedown="isButtonActive = true"
                @mouseup="isButtonActive = false"
                @mouseleave="isButtonActive = false"
              >
                <span v-if="!isLoading">Обработать файл</span>
                <span v-else>
                  <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                  Обработка...
                </span>
              </button>
            </div>
          </div>
        </form>
      </div>
      <hr size="5" noshade color="#768c8c">
    </div>
    <div class="container text-left">
      <div v-if="referenceFile">
        <h2 :style="{ color: 'green' }">{{ referenceFileName }} {{ referenceFileRecordsCount }} товаров </h2>
      </div>
      <div v-else>
        <h2 :style="{ color: 'red' }">Не загружен файл - справочник</h2>
      </div>
      <div>
        <ol>
          <li v-for="file in files" :key="file">
            <a :href="file" target="_blank">{{ file }}</a>
          </li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      referenceFile: false,
      referenceFileName: '',
      referenceFileRecordsCount: 0,
      files: [],
      selectedFile: null,
      processSecondBarcode: true,
      isLoading: false,
      isRestarting: false,
      isButtonActive: false
    };
  },
  methods: {
    async onFileChange(event) {
      this.selectedFile = event.target.files[0];
      if (this.selectedFile) {
        await this.handleFileUpload();
      }
    },
    async handleFileUpload() {
      if (this.selectedFile) {
        this.isLoading = true;
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        try {
          const response = await axios.post('/api/upload', formData);
          this.referenceFile = true;
          this.referenceFileName = response.data.fileName;
          await this.fetchFiles();
        } catch (error) {
          console.error('Ошибка при загрузке файла:', error);
        } finally {
          this.isLoading = false;
          this.selectedFile = null;
        }
      }
    },
    async fetchFiles() {
      try {
        const response = await axios.get('/api/files');
        this.files = response.data.files;
        this.referenceFile = response.data.referenceFile;
        this.referenceFileName = response.data.referenceFileName;
        this.referenceFileRecordsCount = response.data.count;
      } catch (error) {
        console.error('Ошибка при получении списка файлов:', error);
      }
    },
    async restartApplication() {
      if (confirm('Вы уверены, что хотите перезапустить приложение? Это может занять несколько секунд.')) {
        this.isRestarting = true;
        try {
          const response = await axios.post('/restartApplication');
          this.$toast.success(response.data || 'Приложение перезапускается...', {
            position: 'top-right',
            timeout: 5000
          });
          
          // Обновляем страницу через 5 секунд
          setTimeout(() => {
            window.location.reload();
          }, 5000);
        } catch (error) {
          console.error('Ошибка при перезапуске:', error);
          this.$toast.error(error.response?.data || 'Ошибка при перезапуске приложения', {
            position: 'top-right',
            timeout: 5000
          });
        } finally {
          this.isRestarting = false;
        }
      }
    }
  },
  mounted() {
    this.fetchFiles();
  }
};
</script>

<style scoped>
.sticky-top {
  align-items: normal;
  text-align: left;
  position: sticky;
  top: 0;
  background-color: white;
  z-index: 1000;
  width: 100%;
}

html {
  font-size: 100%;
}

body {
  font-size: 1.25em;
  margin: 0;
  padding: 0;
  top: 0%;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  height: 100vh;
  width: 100%;
}

.container {
  width: 100%;
  align-items: flex-start;
  margin: 0;
  top: 0%;
  text-align: left;
}

h1 {
  font-size: 25px;
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.form-cell {
  flex: 1;
  text-align: left;
  padding: 10px;
}

.form-cell-right {
  text-align: right;
  padding: 10px;
  margin-left: auto;
}

.file-upload-container {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.file-name {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.custom-file-upload {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  color: #fff;
  background-color: #007bff;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
}

.custom-file-upload:hover {
  background-color: #007bff;
}

.custom-file-upload input[type="file"] {
  display: none;
}

.spinner-border {
  vertical-align: middle;
  margin-right: 5px;
}

/* Анимация пульсирующих точек */
.pulsating-dots {
  display: inline-block;
}

.pulsating-dots .dot {
  display: inline-block;
  animation: pulse 1.4s infinite;
  opacity: 0;
  font-size: 24px; /* Увеличиваем размер точек */
}

.pulsating-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.pulsating-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

/* Button hover and active animations */
.btn-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.btn-active:active {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.1s ease;
}

/* Restart button specific styles */
.btn-outline-danger {
  transition: all 0.3s ease;
  border-width: 2px;
}

.btn-outline-danger:hover {
  background-color: rgba(220, 53, 69, 0.1);
  transform: scale(1.05);
}

.btn-outline-danger:active {
  transform: scale(0.98);
}

/* Make sure the button doesn't jump during state changes */
button {
  transition: all 0.2s ease;
}
</style>