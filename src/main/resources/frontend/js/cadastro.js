
document.addEventListener('DOMContentLoaded', function() {

  
  const formCadastro = document.querySelector('.cadastro-form');
  
  formCadastro.addEventListener('submit', function(e) {
      e.preventDefault(); // Impede o envio do formulário
      
      // Validação dos campos
      const nome = formCadastro.querySelector('input[placeholder="Nome completo"]').value;
      const login = formCadastro.querySelector('input[placeholder="login"]').value;
      const email = formCadastro.querySelector('input[placeholder="E-mail"]').value;
      const senha = formCadastro.querySelector('input[placeholder="Senha"]').value;
      const cpf = formCadastro.querySelector('input[placeholder="cpf"]').value;
      const confirmaSenha = formCadastro.querySelector('input[placeholder="Confirme sua senha"]').value;
      
      let mensagemErro = '';
      if (!nome || !email || !senha || !confirmaSenha || !cpf || !login) {
          mensagemErro = 'Todos os campos são obrigatórios.';
      } else if (senha !== confirmaSenha) {
          mensagemErro = 'As senhas não coincidem.';
      } else if (!email.match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
          mensagemErro = 'Por favor, insira um e-mail válido.';
      }
      
      if (mensagemErro) {
          alert(mensagemErro); 
      } else {

          const dadosUsuario = {
              nome: nome,
              login: login,
              email: email,
              cpf: cpf,
              senha: senha
          };


          fetch('/newCustomer', {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify(dadosUsuario)
          })
              .then(response => response.json())
              .then(data => {
                  console.log(data);
              })
              .catch(error => {
                  console.error('Erro:', error);
              });

          console.log('Formulário válido, enviar dados...');
      }
  });
});


