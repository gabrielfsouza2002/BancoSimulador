document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard carregado com sucesso!');
    const saldoAtual = document.getElementById('valorSaldo');

    if (saldoAtual) {
        let saldo = parseFloat(saldoAtual.innerText.replace('R$', '').replace(',', '.'));

        function atualizarSaldo(valor, operacao) {
            if (operacao === 'subtrair') {
                saldo -= valor;
            } else if (operacao === 'adicionar') {
                saldo += valor;
            }
            saldoAtual.innerText = `R$ ${saldo.toFixed(1).replace(',', '.')}`;
        }

        document.getElementById('formTransferencia').addEventListener('submit', function(e) {
            console.log('Botão foi pressionado com sucesso');
            e.preventDefault();
            const valor = parseFloat(document.getElementById('valorTransferencia').value);
            const contaDestino = document.getElementById('contaDestino').value;

            if (valor > 0 && saldo >= valor) {
                console.log(`Valor: ${valor}, Conta Destino: ${contaDestino}`)
                fetch('/transfer', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({
                        valor: valor,
                        contaDestino: contaDestino
                    })
                })
                    .then(response => {
                        if (response.ok) {
                            atualizarSaldo(valor, 'subtrair');
                            alert('Transferência realizada com sucesso!');
                        } else {
                            alert('Falha na transferência (Erro de Servidor ou Tentativa de transação para própria conta.)');
                        }
                    })
                    .catch(error => {
                        console.error('Erro:', error);
                        alert('Erro ao realizar a transferência.');
                    });
            } else {
                alert('Saldo insuficiente ou valor inválido.');
            }
        });
    } else {
        console.error('Elemento saldoAtual não encontrado.');
    }
});