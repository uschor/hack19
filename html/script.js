var app = new Vue({
    el: '#app',
    data: {
        text: '',
        results:[]
    },
    methods: {
        query() {
            debugger
            axios.get('/user?ID=12345')
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