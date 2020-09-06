package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.RelogioDoSistema;
import br.com.caelum.leilao.infra.dao.LeilaoRepositorio;
import br.com.caelum.leilao.infra.dao.PagamentosRepository;

import java.util.Calendar;
import java.util.List;

public class GeradorDePagamento {


    private final LeilaoRepositorio leiloes;
    private final Avaliador avaliador;
    private final PagamentosRepository pagamento;
    private final RelogioDoSistema relogio;

    public GeradorDePagamento(LeilaoRepositorio leiloes, Avaliador avaliador, PagamentosRepository pagamento,
                              RelogioDoSistema relogio){
        this.leiloes = leiloes;
        this.avaliador = avaliador;
        this.pagamento = pagamento;
        this.relogio = relogio;
    }

    public GeradorDePagamento(LeilaoRepositorio leiloes, Avaliador avaliador, PagamentosRepository pagamento){
        this(leiloes, avaliador, pagamento, new RelogioDoSistema());
    }

    public void gera() {
        List<Leilao> leiloesEncerrados = this.leiloes.encerrados();

        for(Leilao l : leiloesEncerrados){
            this.avaliador.avalia(l);

            Pagamento pagamento = new Pagamento(avaliador.getMaiorLance(), primeiroDiaUtil());
            this.pagamento.salva(pagamento);
        }
    }

    private Calendar primeiroDiaUtil() {
        Calendar data = relogio.hoje();
        int diaDaSemana = data.get(Calendar.DAY_OF_WEEK);

        if(diaDaSemana == Calendar.SATURDAY){
            data.add(Calendar.DAY_OF_WEEK, 2);

        } else if(diaDaSemana == Calendar.SUNDAY) {
            data.add(Calendar.DAY_OF_WEEK, 1);
        }

        return data;
    }
}
