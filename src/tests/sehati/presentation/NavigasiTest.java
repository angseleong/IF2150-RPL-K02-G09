package sehati.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;

class NavigasiTest {

    private static final String NS_FXML = "http://javafx.com/fxml/1";

    /**
     * FXML dibaca sebagai XML karena runner CI tidak memiliki layar untuk toolkit JavaFX.
     */
    @Test
    void judul_setiapLayarPunyaFxmlDenganControllerForm() throws Exception {
        assertEquals(15, Navigasi.JUDUL.size());
        DocumentBuilderFactory pabrik = DocumentBuilderFactory.newInstance();
        pabrik.setNamespaceAware(true);

        for (Map.Entry<String, String> layar : Navigasi.JUDUL.entrySet()) {
            String kelas = "sehati.presentation." + layar.getKey();
            try (InputStream fxml = getClass().getResourceAsStream("/sehati/fxml/" + layar.getKey() + ".fxml")) {
                assertNotNull(fxml, layar.getKey());
                String controller = pabrik.newDocumentBuilder().parse(fxml).getDocumentElement()
                        .getAttributeNS(NS_FXML, "controller");
                assertEquals(kelas, controller);
            }
            Class.forName(kelas).getConstructor();
        }
    }

    @Test
    void buka_menolakLayarTidakDikenal() {
        assertThrows(IllegalArgumentException.class, () -> Navigasi.buka("LayarTidakAda"));
    }

    @Test
    void tema_tersedia() {
        assertNotNull(getClass().getResource("/sehati/css/sehati.css"));
    }
}
