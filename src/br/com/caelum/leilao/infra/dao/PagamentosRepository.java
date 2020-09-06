package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Pagamento;

public interface PagamentosRepository {

    void salva(Pagamento pagamento);
}
