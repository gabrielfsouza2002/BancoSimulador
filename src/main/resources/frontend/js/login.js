document.addEventListener('DOMContentLoaded', function() {
    const formLogin = document.querySelector('.cadastro-form');

    formLogin.addEventListener('submit', function(e) {
        e.preventDefault(); // Impede o envio do formulário

        // Validação dos campos
        const senha = formLogin.querySelector('input[placeholder="Senha"]').value;
        const login = formLogin.querySelector('input[placeholder="login"]').value;

        const loginData = {
            username: login,
            password: senha
        };

        fetch('/loginDate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        })
            .then(response => response.json())
            .then(data => {
                console.log(data);
            })
            .catch(error => {
                console.error('Erro:', error);
            });
        console.log(loginData)
        console.log('Formulário válido, enviar dados...');

    });
});