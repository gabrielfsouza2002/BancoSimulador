document.addEventListener('DOMContentLoaded', function() {
  const formLogin = document.querySelector('.cadastro-form');

  formLogin.addEventListener('submit', function(e) {
    e.preventDefault(); // Impede o envio do formulário

    // Validação dos campos
    const email = formLogin.querySelector('input[placeholder="E-mail"]').value;
    const senha = formLogin.querySelector('input[placeholder="Senha"]').value;
    const login = formLogin.querySelector('input[placeholder="login"]').value;

    let mensagemErro = '';
    if (!email || !senha || !login) {
      mensagemErro = 'Todos os campos são obrigatórios.';
    } else if (!email.match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
      mensagemErro = 'Por favor, insira um e-mail válido.';
    }

    if (mensagemErro) {
      alert(mensagemErro); // Substitua por uma implementação mais elegante conforme necessário
    } else {
      // Aqui você pode adicionar o código para enviar os dados do formulário
      console.log('Formulário válido, enviar dados...');
    }
  });
});