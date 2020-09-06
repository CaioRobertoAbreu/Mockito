package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.EnviadorDeEmailRepositorio;
import br.com.caelum.leilao.infra.dao.LeilaoRepositorio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.JANUARY;
import static java.util.Calendar.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EncerradorDeLeilaoTest {

    private Calendar antiga;
    private Calendar hoje;
    Leilao leilao;
    private List<Leilao> leiloes;

    @BeforeEach
    public void setup() {
        antiga = getInstance();
        antiga.set(2019, JANUARY, 20);
        hoje = getInstance();

        leilao = new CriadorDeLeilao().para("PS 4").naData(antiga).constroi();
        leiloes = new ArrayList<>();
        leiloes.add(leilao);

    }


    @Test
    @DisplayName("Deve encerrar leilões que iniciaram há uma semana")
    public void deveEncerrarLeiloes() {

        Leilao leilao2 = new CriadorDeLeilao().para("PS 4").naData(hoje).constroi();
        leiloes.add(leilao2);


        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(leiloes);

        EnviadorDeEmailRepositorio enviadorDeEmail = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoMock, enviadorDeEmail);

        encerradorDeLeilao.encerra();

        Assertions.assertTrue(leilao.isEncerrado());
        assertEquals(encerradorDeLeilao.getTotalEncerrados(), 1);

        //Retorna LeiloesAntigos
        assertEquals(daoMock.correntes().size(), 2);

    }



    @Test
    @DisplayName("Verifica se o método atualiza está deixando o leilao " +
            "como encerrado")
    public  void verificaSeAtualizaStatusEncerrado(){

        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(leiloes);

        EnviadorDeEmailRepositorio enviadorDeEmail = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock, enviadorDeEmail);
        encerrador.encerra();

        Mockito.verify(daoMock, Mockito.times(1)).atualiza(leilao);

        Assertions.assertTrue(leilao.isEncerrado());
    }

    @Test
    @DisplayName("Verifica se o leilao foi encerrado")
    public void verificaLeilaEncerrado(){

        LeilaoRepositorio repositorioMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(repositorioMock.correntes()).thenReturn(leiloes);

        EnviadorDeEmailRepositorio enviadorDeEmail = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(repositorioMock, enviadorDeEmail);
        encerrador.encerra();

        Assertions.assertTrue(leilao.isEncerrado());
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        EnviadorDeEmailRepositorio enviadorDeEmail = Mockito.mock(EnviadorDeEmailRepositorio.class);


        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock, enviadorDeEmail);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());

        Mockito.verify(daoMock, Mockito.never()).atualiza(leilao1);
        Mockito.verify(daoMock, Mockito.never()).atualiza(leilao2);
    }

    @Test
    @DisplayName("Verifica se email é enviado ao encerrar leiloes")
    public void verificaEnvioDeEmail() {

        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(leiloes);

        EnviadorDeEmailRepositorio enviaEmailMock = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoMock, enviaEmailMock);
        encerradorDeLeilao.encerra();

        Mockito.verify(enviaEmailMock).envia(leilao);
    }

    @Test
    @DisplayName("Verificar se os testes são executados na ordem")
    public void deveExecutarTestesNaOrdemCorreta() {
        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(leiloes);

        EnviadorDeEmailRepositorio enviaEmailMock = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoMock, enviaEmailMock);
        encerradorDeLeilao.encerra();

        InOrder ordem = Mockito.inOrder(daoMock, enviaEmailMock);

        //Aproveitei para verificar a quantidade de vezes que o método atualiza foi chamado.
        ordem.verify(daoMock, Mockito.times(1)).atualiza(leilao);
        ordem.verify(enviaEmailMock).envia(leilao);
    }

    @Test
    @DisplayName("Verifica se a excecao é capturada e continuou o envio de emais" +
            "lançar algum erro")
    public void deveContinuarEnviandoEmailQuandoHouverExcecoes() {
        Leilao novoLeilao = new CriadorDeLeilao().para("PS 5").naData(antiga).constroi();
        leiloes.add(novoLeilao);

        LeilaoRepositorio daoMock = Mockito.mock(LeilaoRepositorio.class);
        Mockito.when(daoMock.correntes()).thenReturn(leiloes);

        Mockito.doThrow(new RuntimeException()).when(daoMock).atualiza(leilao);

        EnviadorDeEmailRepositorio enviaEmailMock = Mockito.mock(EnviadorDeEmailRepositorio.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoMock, enviaEmailMock);
        encerradorDeLeilao.encerra();

        Mockito.verify(daoMock).atualiza(novoLeilao);
        Mockito.verify(enviaEmailMock).envia(novoLeilao);

    }

}


