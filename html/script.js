var app = new Vue({
    el: '#app',
    data: {
        text: '',
        results:[]
    },
    methods: {
        query() {
            //var input='ואת שתי קצות שתי העבותות תיתן';
            var input= document.getElementById('input').value;
            debugger
            axios({
                method: 'post',
                url: 'http://localhost:9999/identify',
                headers: {'Content-Type': 'text/plain'},
                data:input
            })
            // axios.post('http://localhost:9999/identify')
                .then(function (response) {
                    document.getElementById('result').innerHTML=response.data;
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