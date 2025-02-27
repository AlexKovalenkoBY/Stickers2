<template>
  <div>
    <div class="header text-left sticky-top">
      <h3>Сервиc формирования стикеров на основании XLSx файлов</h3>
      <div>
        <span>Краткая инструкция:</span> <br>
        <span>1. загрузить справочник (большой файл)</span><br>
        <span>2. загрузить файл заказа. После выбора файла обработка начнется автоматически.</span><br>
        <span>3. посмотреть/распечатать файл из списка </span><br>
      </div>
      <div>
        <form @submit.prevent="handleFileUpload" enctype="multipart/form-data">
          <div class="form-row">
            <div class="form-cell file-upload-container">
              <span>Выберите файл для загрузки:</span>
              <label class="btn btn-primary custom-file-upload">
                <input type="file" name="file" accept=".xlsx" @change="onFileChange" /> Выбрать файл
              </label>
              <span v-if="selectedFile" class="file-name">{{ selectedFile.name }}</span>
              <span v-else class="file-name">Файл не выбран</span>
            </div>
            <div class="form-cell-right">
              <button type="submit" class="btn btn-primary" :disabled="isLoading">
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
        <h2 :style="{ color: 'green' }">{{ referenceFileName }}.  Загружено {{ referenceFileRecordsCount }} товаров </h2>
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
      isLoading: false
    };
  },
  methods: {
    async onFileChange(event) {
      this.selectedFile = event.target.files[0];
      if (this.selectedFile) {
        await this.handleFileUpload(); // Используем await для ожидания завершения handleFileUpload
      }
    },
    async handleFileUpload() {
      if (this.selectedFile) {
        this.isLoading = true;
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        try {
          const response = await axios.post('http://127.0.0.1:8081/api/upload', formData);
          this.referenceFile = true;
          this.referenceFileName = response.data.fileName;
          await this.fetchFiles(); // Обновляем список файлов после успешной загрузки
        } catch (error) {
          console.error('Ошибка при загрузке файла:', error);
        } finally {
          this.isLoading = false;
        }
      }
    },
    async fetchFiles() {
      try {
        const response = await axios.get('http://127.0.0.1:8081/api/files');
        this.files = response.data.files; // Список файлов уже отсортирован на бэкенде
        this.referenceFile = response.data.referenceFile;
        this.referenceFileName = response.data.referenceFileName;
        this.referenceFileRecordsCount = response.data.count;

        console.log("referenceFileName - ", this.referenceFileName);
      } catch (error) {
        console.error('Ошибка при получении списка файлов:', error);
      }
    },
    fetchReferenceFileStatus() {
      axios.get('http://127.0.0.1:8081/api/getReferenceFileRecordsCount')
        .then(response => {
          this.referenceFile = response.data.status;
          if (this.referenceFile) {
            this.referenceFileName = response.data.referenceFileName;
            this.referenceFileRecordsCount = response.data.referenceFileRecordsCount;
          }
        })
        .catch(error => {
          console.error('Ошибка при получении статуса справочника:', error);
        });
    }
  },
  mounted() {
    this.fetchFiles();
   // this.fetchReferenceFileStatus();
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
  background-color: #0056b3;
}

.custom-file-upload input[type="file"] {
  display: none;
}

.spinner-border {
  vertical-align: middle;
  margin-right: 5px;
}
</style>