package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.RelogioDoSistema;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.LeilaoRepositorio;
import br.com.caelum.leilao.infra.dao.PagamentosRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GeradorPagamentoTest {

    private List<Leilao> leiloes;

    @BeforeEach
    public void setup(){
        leiloes = new ArrayList<>();
    }

    @Test
    @DisplayName("Gerar pagamentos para um leilao encerrado")
    public void deveGerarPagmentoDeUmLeilaoEncerrado(){
        Leilao leilao = new CriadorDeLeilao().para("PS 4")
                .lance(new Usuario("Pedro Alves"), 2400.)
                .lance(new Usuario("Maria Joaquina"), 3300.)
                .constroi();

        leiloes.add(leilao);


        LeilaoRepositorio leilaoMock = Mockito.mock(LeilaoRepositorio.class);
        Avaliador avaliador = new Avaliador();
        //Como não faz acesso para fora da app (ex.: Banco de Dados) então não há necessidade de Mockar
        PagamentosRepository pagamentosMock = Mockito.mock(PagamentosRepository.class);

        Mockito.when(leilaoMock.encerrados()).thenReturn(leiloes);

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leilaoMock, avaliador, pagamentosMock);

        geradorDePagamento.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        Mockito.verify(pagamentosMock).salva(argumento.capture());

        Pagamento pagamentoCapture = argumento.getValue();

        Assertions.assertEquals(3300, pagamentoCapture.getValor());
    }

    @Test
    @DisplayName("Deve gerar pagamento no próximo dia útil caso seja sábado ou domingo")
    public void deveGerarPagamentoNoProximoDiaUtilCasoFimDeSemana(){
        Leilao novoLeilao = new CriadorDeLeilao().para("Iphone X")
                .lance(new Usuario("Ricardo"), 6000.)
                .lance(new Usuario("Geovana"), 6150.)
                .constroi();

        leiloes.add(novoLeilao);

        LeilaoRepositorio leilaoRepositorioMock = Mockito.mock(LeilaoRepositorio.class);
        Avaliador avalia = new Avaliador();
        PagamentosRepository pagamentosRepositoryMock = Mockito.mock(PagamentosRepository.class);
        RelogioDoSistema relogioMock = Mockito.mock(RelogioDoSistema.class);

        Calendar sabado = Calendar.getInstance();
        sabado.set(2012, Calendar.APRIL, 7);

        Mockito.when(leilaoRepositorioMock.encerrados()).thenReturn(leiloes);
        Mockito.when(relogioMock.hoje()).thenReturn(sabado);

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leilaoRepositorioMock, avalia,
                pagamentosRepositoryMock, relogioMock);
        geradorDePagamento.gera();

        ArgumentCaptor<Pagamento> capturador = ArgumentCaptor.forClass(Pagamento.class);
        Mockito.verify(pagamentosRepositoryMock).salva(capturador.capture());

        Pagamento pagamentoCapturado = capturador.getValue();

        Assertions.assertEquals(Calendar.MONDAY, pagamentoCapturado.getData().get(Calendar.DAY_OF_WEEK));

    }
}
