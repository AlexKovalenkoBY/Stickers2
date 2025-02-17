<template>
  <div>
    <div class="header text-left sticky-top">
      <h3>Сервиc формирования стикеров на основании XLSx файлов</h3>
      <div>
        <span>Краткая инструкция:</span> <br>
        <span>1. загрузить справочник (большой файл)</span><br>
        <span>2. загрузить файл заказа</span><br>
        <span>3. посмотреть/распечатать файл из списка </span><br>
      </div>
      <div>
        <form @submit.prevent="handleFileUpload" enctype="multipart/form-data">
          <table>
            <tbody>
              <tr>
                <td style="width: 420px;">Выберите файл для загрузки:</td>
                <td style="width: 420px;"><input type="file" name="file" accept=".xlsx" @change="onFileChange" /></td>
                <td style="width: 420px;"><button type="submit" class="btn btn-primary">Обработать файл</button></td>
              </tr>
            </tbody>
          </table>
        </form>
      </div>
      <hr size="5" noshade color="#768c8c">
    </div>
    <div class="container text-left">
      <div v-if="referenceFile">
        <h2 :style="{ color: 'green' }">{{ referenceFileName }}</h2>
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
import axios from 'axios'; // Импортируем axios для HTTP-запросов

export default {
  data() {
    return {
      referenceFile: false,
      referenceFileName: '',
      files: [],
      selectedFile: null
    };
  },
  methods: {
    onFileChange(event) {
      this.selectedFile = event.target.files[0];
    },
    handleFileUpload() {
      if (this.selectedFile) {
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        // Отправка файла на сервер
        axios.post('http://127.0.0.1:8081/api/upload', formData)
          .then(response => {
            this.referenceFile = true;
            this.referenceFileName = response.data.fileName;
            this.files = response.data.files;

            // Вызов fetchFiles() после успешной загрузки файла
            this.fetchFiles();
          })
          .catch(error => {
            console.error('Ошибка при загрузке файла:', error);
          });
      }
    },
    fetchFiles() {
      // Запрос списка файлов
      axios.get('http://127.0.0.1:8081/api/files')
        .then(response => {
          this.files = response.data.files;
          this.referenceFile = response.data.referenceFile;
          this.referenceFileName = response.data.referenceFileName;
        })
        .catch(error => {
          console.error('Ошибка при получении списка файлов:', error);
        });
    },
    fetchReferenceFileStatus() {
      // Запрос статуса справочника
      axios.get('http://127.0.0.1:8081/api/referenceFileStatus')
        .then(response => {
          this.referenceFile = response.data.status;
          if (this.referenceFile) {
            this.referenceFileName = response.data.referenceFileName;
          }
        })
        .catch(error => {
          console.error('Ошибка при получении статуса справочника:', error);
        });
    }
  },
  mounted() {
    // Вызов методов при монтировании компонента
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
}

.container {
  width: 100%;
  max-width: 80%;
  align-items: flex-start;
  margin: 0;
  top: 0%;
  text-align: left;
}

h1 {
  font-size: 25px;
}

table {
  margin: 0;
  padding: 0;
}

ol {
  padding-left: 20px;
}
</style>