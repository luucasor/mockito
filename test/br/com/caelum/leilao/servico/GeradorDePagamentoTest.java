package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import org.junit.Test;
import sun.security.x509.AVA;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeradorDePagamentoTest {

    @Test
    public void deveGerarPagamentoParaUmLeilaoEncerrado(){
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        Avaliador avaliador = mock(Avaliador.class);

        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José"), 2000.0)
                .lance(new Usuario("Maria"), 2500.0)
                .constroi();

        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
        when(avaliador.getMaiorLance()).thenReturn(2500.0);

        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
        gerador.gera();
    }
}
