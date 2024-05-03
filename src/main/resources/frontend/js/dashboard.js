document.addEventListener('DOMContentLoaded', function() {
  atualizarSaldo(5000); // Simula a atualização para um saldo de R$ 5.000,00
});



document.addEventListener('DOMContentLoaded', function() {
  const saldoAtual = document.getElementById('valorSaldo');
  // Extrai o valor numérico do saldo atual do texto
  let saldo = parseFloat(saldoAtual.innerText.replace('R$', '').replace(',', '.'));
  
  function atualizarSaldo(valor, operacao) {
      if (operacao === 'subtrair') {
          saldo -= valor;
      } else if (operacao === 'adicionar') {
          saldo += valor;
      }
      // Atualiza o texto do elemento saldoAtual com o novo saldo
      saldoAtual.innerText = `R$ ${saldo.toFixed(2).replace('.', ',')}`;
  }

  function adicionarTransacaoAoHistorico(valor, tipo) {
    const listaTransacoes = document.querySelector('.lista-transacoes');
    const novaTransacao = document.createElement('li');
    novaTransacao.classList.add('transacao', tipo);
    const dataAtual = new Date().toLocaleDateString('pt-BR');
    novaTransacao.innerHTML = `<span>${tipo === 'despesas' ? 'Pagamento' : 'Transferência enviada'}: ${tipo === 'despesas' ? '-' : '+'}R$ ${valor.toFixed(2).replace('.', ',')}</span><span class="data">${dataAtual}</span>`;

    // Verifica se a lista já tem itens e insere a nova transação no topo
    if (listaTransacoes.firstChild) {
        listaTransacoes.insertBefore(novaTransacao, listaTransacoes.firstChild);
    } else {
        listaTransacoes.appendChild(novaTransacao);
    }
}

  document.getElementById('formTransferencia').addEventListener('submit', function(e) {
      e.preventDefault();
      const valor = parseFloat(document.getElementById('valorTransferencia').value);
      if (valor > 0 && saldo >= valor) {
          atualizarSaldo(valor, 'subtrair');
          adicionarTransacaoAoHistorico(valor, 'despesas');
          alert('Transferência realizada com sucesso!');
      } else {
          alert('Saldo insuficiente ou valor inválido.');
      }
  });

});
 /* document.getElementById('formPagamento').addEventListener('submit', function(e) {
    e.preventDefault();
    // Usa o valor do novo campo de entrada para o valor do pagamento
    const valor = parseFloat(document.getElementById('valorPagamento').value);
    if (valor > 0 && saldo >= valor) {
        atualizarSaldo(valor, 'subtrair');
        adicionarTransacaoAoHistorico(valor, 'despesas');
        alert('Pagamento realizado com sucesso!');
    } else {
        alert('Saldo insuficiente ou valor inválido.');
    }
});*/

/*document.getElementById('formCartaoCredito').addEventListener('submit', function(e) {
    e.preventDefault();
    const numeroCartao = document.getElementById('numeroCartao').value;
    const nomeCartao = document.getElementById('nomeCartao').value;
    const validadeCartao = document.getElementById('validadeCartao').value;
    const cvvCartao = document.getElementById('cvvCartao').value;
    // Validação e salvamento das informações do cartão de crédito
    // ...
    alert('Informações do cartão de crédito salvas com sucesso!');
});*/

