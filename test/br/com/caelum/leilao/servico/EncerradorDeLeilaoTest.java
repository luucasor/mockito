package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class EncerradorDeLeilaoTest {

    Calendar antiga;
    Leilao leilao1;
    Leilao leilao2;
    List<Leilao> leiloesAntigos;
    RepositorioDeLeiloes daoFalso;
    EnviadorDeEmail carteiroFalso;
    EncerradorDeLeilao encerrador;

    @Before
    public void setUp(){
        antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();
        leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
        leiloesAntigos = Arrays.asList(leilao1, leilao2);

        daoFalso = mock(RepositorioDeLeiloes.class);
        carteiroFalso = mock(EnviadorDeEmail.class);
        encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
    }

    @Test
    public  void deveEncerrarLeiloesQueComecaramUmaSemanaAtras(){
        when(daoFalso.correntes()).thenReturn(leiloesAntigos);
        encerrador.encerra();

        assertEquals(2, encerrador.getTotalEncerrados());
        assertTrue(leilao1.isEncerrado());
        assertTrue(leilao2.isEncerrado());
    }

    @Test
    public void naoDeveEncerrarLeiloesIniciadosOntem(){
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilaoInterno1 = new CriadorDeLeilao().para("TV de Plasma").naData(ontem).constroi();
        Leilao leilaoInterno2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();
        List<Leilao> leiloesOntem = Arrays.asList(leilaoInterno1, leilaoInterno2);

        when(daoFalso.correntes()).thenReturn(leiloesOntem);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilaoInterno1.isEncerrado());
        assertFalse(leilaoInterno2.isEncerrado());
    }

    @Test
    public void casoNaoHajaNenhumLeilaoEncerradorNaoFazNada(){
        when(daoFalso.correntes()).thenReturn(new ArrayList<>());
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
    }

    @Test
    public void deveAtualizarLeiloesEncerrados(){
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
        encerrador.encerra();

        verify(daoFalso, times(1)).atualiza(leilao1);
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilaoInterno1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(ontem).constroi();
        Leilao leilaoInterno2 = new CriadorDeLeilao().para("Geladeira")
                .naData(ontem).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilaoInterno1, leilaoInterno2));
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilaoInterno1.isEncerrado());
        assertFalse(leilaoInterno2.isEncerrado());

        verify(daoFalso, never()).atualiza(any());
    }

    @Test
    public void deveValidarSeEmailRealmenteFoiEnviado(){
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
        encerrador.encerra();

        InOrder inOrder = inOrder(daoFalso, carteiroFalso);
        inOrder.verify(daoFalso, atLeastOnce()).atualiza(any());
        inOrder.verify(carteiroFalso, atLeastOnce()).envia(any());
    }

    @Test
    public void deveContinuarAExecucaoMesmoQuandoDaoFalha(){
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);

        encerrador.encerra();

        verify(daoFalso).atualiza(leilao2);
        verify(carteiroFalso).envia(leilao2);
        verify(carteiroFalso, times(0)).envia(leilao1);
    }
    
}
