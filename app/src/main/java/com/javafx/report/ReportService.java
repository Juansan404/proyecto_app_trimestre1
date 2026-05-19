package com.javafx.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.export.*;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Genera informes PDF programáticamente usando JasperReports 7.
 * No requiere archivos .jrxml externos: el diseño se construye en tiempo de ejecución.
 */
public class ReportService {

    private static final Color ACCENT     = new Color(233, 69, 96);
    private static final Color DARK       = new Color(22, 26, 46);
    private static final Color DARK_TEXT  = new Color(40, 46, 80);
    private static final Color GRAY_TEXT  = new Color(100, 105, 140);
    private static final Color ALT_ROW    = new Color(245, 246, 252);
    private static final Color BORDER_COL = new Color(215, 218, 232);

    /** Ancho de página A4 apaisado en puntos. */
    private static final int PAGE_W = 842;
    private static final int PAGE_H = 595;
    private static final int MAR_H  = 40;
    private static final int USABLE = PAGE_W - MAR_H * 2;   // 762

    /**
     * @param subtitulo Tipo de informe (p.ej. "Informe de Citas")
     * @param filtros   Descripción de los filtros activos, o cadena vacía
     * @param etiquetas Cabeceras de columna visibles al usuario
     * @param campos    Claves de los mapas de datos (mismo orden que etiquetas)
     * @param datos     Filas: cada fila es un Map&lt;campo → valor&gt;
     * @return          Bytes del PDF generado
     */
    public byte[] generar(String subtitulo,
                          String filtros,
                          List<String> etiquetas,
                          List<String> campos,
                          List<Map<String, Object>> datos) throws Exception {

        final int[] ws = distribuir(USABLE, etiquetas.size());

        JasperDesign d = new JasperDesign();
        d.setName("informe");
        d.setPageWidth(PAGE_W);  d.setPageHeight(PAGE_H);
        d.setOrientation(OrientationEnum.LANDSCAPE);
        d.setLeftMargin(MAR_H);  d.setRightMargin(MAR_H);
        d.setTopMargin(10);      d.setBottomMargin(30);
        d.setColumnWidth(USABLE);

        // ── Parámetros ──────────────────────────────────────────────────────
        addParam(d, "SUBTITULO",    String.class);
        addParam(d, "FILTROS",      String.class);
        addParam(d, "GENERADO_EN",  String.class);
        addParam(d, "TOTAL",        String.class);
        addParam(d, "LOGO",         java.io.InputStream.class);

        // ── Campos ──────────────────────────────────────────────────────────
        for (String campo : campos) {
            JRDesignField f = new JRDesignField();
            f.setName(campo);
            f.setValueClass(String.class);
            d.addField(f);
        }

        // ── Bandas ──────────────────────────────────────────────────────────
        d.setTitle(buildTitle(USABLE));
        d.setColumnHeader(buildColumnHeader(etiquetas, ws, USABLE));
        ((JRDesignSection) d.getDetailSection()).addBand(buildDetail(campos, ws, USABLE));
        d.setPageFooter(buildPageFooter(USABLE));

        // ── Directorio temporal para JasperReports (evita escribir en Program Files) ──
        String appData = System.getenv("APPDATA");
        String tempBase = appData != null ? appData : System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempBase, "TattooAge" + File.separator + "temp");
        tempDir.mkdirs();
        DefaultJasperReportsContext jrCtx = DefaultJasperReportsContext.getInstance();
        jrCtx.setProperty(JRCompiler.COMPILER_TEMP_DIR, tempDir.getAbsolutePath());

        // ── Compilar ─────────────────────────────────────────────────────────
        JasperReport report = JasperCompileManager.getInstance(jrCtx).compile(d);

        // ── Parámetros de relleno ─────────────────────────────────────────────
        Map<String, Object> params = new HashMap<>();
        params.put("SUBTITULO", subtitulo);
        params.put("FILTROS",   filtros != null ? filtros : "");
        params.put("GENERADO_EN",
                "Generado: " + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("TOTAL",
                datos.size() + " registro" + (datos.size() != 1 ? "s" : ""));

        // Logo TattooAge
        InputStream logoStream = ReportService.class.getResourceAsStream("/images/logo.png");
        params.put("LOGO", logoStream);  // puede ser null — JRDesignImage lo manejará

        // ── Normalizar datos: todos los valores como String ──────────────────
        List<Map<String, Object>> rows = new ArrayList<>(datos.size());
        for (Map<String, Object> row : datos) {
            Map<String, Object> norm = new LinkedHashMap<>();
            for (String campo : campos) {
                Object v = row.get(campo);
                norm.put(campo, v != null ? v.toString() : "");
            }
            rows.add(norm);
        }

        // Cast necesario: JRMapCollectionDataSource requiere Collection<Map<String,?>>
        @SuppressWarnings("unchecked")
        Collection<Map<String, ?>> ds = (Collection<Map<String, ?>>) (Collection<?>) rows;
        JasperPrint print = JasperFillManager.fillReport(report, params,
                new JRMapCollectionDataSource(ds));

        // ── Exportar a PDF ────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.exportReport();
        return baos.toByteArray();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Construcción de bandas
    // ════════════════════════════════════════════════════════════════════════

    private JRDesignBand buildTitle(int usable) throws JRException {
        JRDesignBand b = new JRDesignBand();
        b.setHeight(82);

        // ── Fondo oscuro + franja accent inferior ─────────────────────────
        b.addElement(rect(0, 0, usable, 82, DARK, 0f));
        b.addElement(rect(0, 78, usable, 4, ACCENT, 0f));

        // ── Separador vertical decorativo entre textos y zona derecha ─────
        b.addElement(rect(usable - 270, 10, 1, 62, new Color(50, 55, 80), 0f));

        // ── Logo TattooAge (esquina derecha, centrado verticalmente) ──────
        JRDesignImage logo = new JRDesignImage(null);
        JRDesignExpression logoExpr = new JRDesignExpression();
        logoExpr.setText("$P{LOGO}");
        logo.setExpression(logoExpr);
        logo.setX(usable - 58); logo.setY(16); logo.setWidth(46); logo.setHeight(46);
        logo.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        logo.setMode(ModeEnum.TRANSPARENT);
        logo.setOnErrorType(OnErrorTypeEnum.BLANK);
        b.addElement(logo);

        // ── "TattooAge" — marca principal (30px alto para caber 20pt)──────
        b.addElement(staticText("TattooAge",
                12, 8, usable - 290, 30,
                20f, true, Color.WHITE, HorizontalTextAlignEnum.LEFT));

        // ── Subtítulo — tipo de informe ───────────────────────────────────
        b.addElement(textField("$P{SUBTITULO}",
                12, 42, usable - 290, 16,
                10f, false, ACCENT, HorizontalTextAlignEnum.LEFT));

        // ── Filtros activos (solo si hay alguno) ──────────────────────────
        JRDesignTextField filtrosField = textField("$P{FILTROS}",
                12, 60, usable - 290, 14,
                8f, false, new Color(140, 145, 180), HorizontalTextAlignEnum.LEFT);
        filtrosField.setBlankWhenNull(true);
        b.addElement(filtrosField);

        // ── Fecha de generación (zona derecha) ────────────────────────────
        b.addElement(textField("$P{GENERADO_EN}",
                usable - 268, 34, 204, 16,
                8f, false, new Color(140, 145, 180), HorizontalTextAlignEnum.RIGHT));

        return b;
    }

    private JRDesignBand buildColumnHeader(List<String> etiquetas,
                                           int[] ws, int usable) {
        JRDesignBand b = new JRDesignBand();
        b.setHeight(26);

        b.addElement(rect(0, 0, usable, 26, ACCENT, 0f));

        int x = 0;
        for (int i = 0; i < etiquetas.size(); i++) {
            b.addElement(staticText(etiquetas.get(i),
                    x + 5, 5, ws[i] - 10, 18,
                    9f, true, Color.WHITE, HorizontalTextAlignEnum.LEFT));
            x += ws[i];
        }
        return b;
    }

    private JRDesignBand buildDetail(List<String> campos, int[] ws, int usable) {
        JRDesignBand b = new JRDesignBand();
        b.setHeight(20);

        // Fondo alterno filas pares
        JRDesignRectangle altBg = rect(0, 0, usable, 20, ALT_ROW, 0f);
        JRDesignExpression altExpr = new JRDesignExpression();
        altExpr.setText("$V{REPORT_COUNT} % 2 == 0");
        altBg.setPrintWhenExpression(altExpr);
        b.addElement(altBg);

        // Línea separadora inferior
        JRDesignLine line = new JRDesignLine();
        line.setX(0); line.setY(19); line.setWidth(usable); line.setHeight(1);
        line.getLinePen().setLineColor(BORDER_COL);
        line.getLinePen().setLineWidth(0.4f);
        b.addElement(line);

        int x = 0;
        for (int i = 0; i < campos.size(); i++) {
            b.addElement(textField("$F{" + campos.get(i) + "}",
                    x + 5, 4, ws[i] - 10, 14,
                    8f, false, DARK_TEXT, HorizontalTextAlignEnum.LEFT));
            x += ws[i];
        }
        return b;
    }

    private JRDesignBand buildPageFooter(int usable) {
        JRDesignBand b = new JRDesignBand();
        b.setHeight(22);

        JRDesignLine line = new JRDesignLine();
        line.setX(0); line.setY(0); line.setWidth(usable); line.setHeight(1);
        line.getLinePen().setLineColor(BORDER_COL);
        line.getLinePen().setLineWidth(0.5f);
        b.addElement(line);

        // Total de registros (izquierda)
        b.addElement(textField("$P{TOTAL}",
                0, 5, 200, 14,
                8f, false, GRAY_TEXT, HorizontalTextAlignEnum.LEFT));

        // Número de página X / Y — evaluado al final del informe
        JRDesignTextField pageField = textField(
                "\"Página \" + $V{PAGE_NUMBER} + \" / \" + $V{PAGE_COUNT}",
                usable - 120, 5, 120, 14,
                8f, false, GRAY_TEXT, HorizontalTextAlignEnum.RIGHT);
        pageField.setEvaluationTime(EvaluationTimeEnum.REPORT);
        b.addElement(pageField);

        return b;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers de elementos gráficos
    // ════════════════════════════════════════════════════════════════════════

    private JRDesignRectangle rect(int x, int y, int w, int h, Color fill, float lineW) {
        JRDesignRectangle r = new JRDesignRectangle();
        r.setX(x); r.setY(y); r.setWidth(w); r.setHeight(h);
        r.setMode(ModeEnum.OPAQUE);
        r.setBackcolor(fill);
        r.getLinePen().setLineWidth(lineW);
        return r;
    }

    private JRDesignTextField textField(String expr, int x, int y, int w, int h,
                                        float size, boolean bold, Color color,
                                        HorizontalTextAlignEnum align) {
        JRDesignTextField tf = new JRDesignTextField();
        JRDesignExpression e = new JRDesignExpression();
        e.setText(expr);
        tf.setExpression(e);
        tf.setX(x); tf.setY(y); tf.setWidth(w); tf.setHeight(h);
        tf.setFontSize(size); tf.setBold(bold);
        tf.setForecolor(color);
        tf.setHorizontalTextAlign(align);
        tf.setMode(ModeEnum.TRANSPARENT);
        tf.setBlankWhenNull(true);
        return tf;
    }

    private JRDesignStaticText staticText(String text, int x, int y, int w, int h,
                                          float size, boolean bold, Color color,
                                          HorizontalTextAlignEnum align) {
        JRDesignStaticText st = new JRDesignStaticText();
        st.setText(text);
        st.setX(x); st.setY(y); st.setWidth(w); st.setHeight(h);
        st.setFontSize(size); st.setBold(bold);
        st.setForecolor(color);
        st.setHorizontalTextAlign(align);
        st.setMode(ModeEnum.TRANSPARENT);
        return st;
    }

    private void addParam(JasperDesign d, String name, Class<?> type) throws JRException {
        JRDesignParameter p = new JRDesignParameter();
        p.setName(name);
        p.setValueClass(type);
        d.addParameter(p);
    }

    /** Distribuye {@code total} puntos entre {@code n} columnas equitativamente. */
    private int[] distribuir(int total, int n) {
        if (n <= 0) return new int[0];
        int[] w = new int[n];
        int base = total / n, extra = total % n;
        for (int i = 0; i < n; i++) w[i] = base + (i < extra ? 1 : 0);
        return w;
    }
}
