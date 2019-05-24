var app = new Vue({
    el: '#app',
    data: {
        text: '',
        results:[]
    },
    methods: {
        query() {
            debugger
            axios.post('localhost:9999/identify')
                .then(function (response) {
                    // handle success
                    console.log(response);
                })
                .catch(function (error) {
                    // handle error
                    console.log(error);
                })
                .finally(function () {
                    // always executed
                });
        }
    }
})