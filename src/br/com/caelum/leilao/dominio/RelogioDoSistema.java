package br.com.caelum.leilao.dominio;

import br.com.caelum.leilao.infra.dao.relogio.RelogioInterface;

import java.util.Calendar;

public class RelogioDoSistema implements RelogioInterface {

    @Override
    public Calendar hoje() {
        return Calendar.getInstance();
    }
}
