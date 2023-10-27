
var dropzone = new Dropzone('#dropzone', {
  // previewTemplate: document.querySelector('#preview-template').innerHTML,
  parallelUploads: 2,
  thumbnailHeight: 120,
  thumbnailWidth: 120,
  maxFilesize: 30,
  filesizeBase: 1000,
  thumbnail: function(file, dataUrl) {
    if (file.previewElement) {
      file.previewElement.classList.remove("dz-file-preview");
      var images = file.previewElement.querySelectorAll("[data-dz-thumbnail]");
      for (var i = 0; i < images.length; i++) {
        var thumbnailElement = images[i];
        thumbnailElement.alt = file.name;
        thumbnailElement.src = dataUrl;
      }
      setTimeout(function() { file.previewElement.classList.add("dz-image-preview"); }, 1);
    }
  }

});


// Now fake the file upload, since GitHub does not handle file uploads
// and returns a 404

var minSteps = 6,
    maxSteps = 60,
    timeBetweenSteps = 10,
    bytesPerStep = 512000;
    Dropzone.autoDiscover = false;
// dropzone.uploadFiles = function(files) {
//   var self = this;

//   for (var i = 0; i < files.length; i++) {

//     var file = files[i];
//     totalSteps = Math.round(Math.min(maxSteps, Math.max(minSteps, file.size / bytesPerStep)));

//     for (var step = 0; step < totalSteps; step++) {
//       var duration = timeBetweenSteps * (step + 1);
//       setTimeout(function(file, totalSteps, step) {
//         return function() {
//           file.upload = {
//             progress: 100 * (step + 1) / totalSteps,
//             total: file.size,
//             bytesSent: (step + 1) * file.size / totalSteps
//           };

//           self.emit('uploadprogress', file, file.upload.progress, file.upload.bytesSent);
//           if (file.upload.progress == 100) {
//             file.status = Dropzone.SUCCESS;
//             self.emit("success", file, 'success', null);
//             self.emit("complete", file);
//             self.processQueue();
//             //document.getElementsByClassName("dz-success-mark").style.opacity = "1";
//           }
//         };
//       }(file, totalSteps, step), duration);
//     }
//     const tt=0;
//   }
// }
dropzone.success =function(file, response)
{
  toastr.success('Файл SCRIPT' + file.name + ' успешно загружен!');
    // Do what you want to do with your response
    // This return statement is necessary to remove progress bar after uploading.
    return file.previewElement.classList.add("dz-success");
}