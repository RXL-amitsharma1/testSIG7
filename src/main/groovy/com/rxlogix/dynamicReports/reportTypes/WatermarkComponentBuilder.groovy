package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.dynamicReports.Templates
import net.sf.dynamicreports.report.base.DRReportTemplate
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.base.style.DRFont
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JasperReportsContext
import net.sf.jasperreports.renderers.AbstractRenderer
import net.sf.jasperreports.renderers.Graphics2DRenderable
import net.sf.jasperreports.renderers.Renderable
import org.jfree.text.TextUtilities
import org.jfree.ui.TextAnchor

import java.awt.*
import java.awt.geom.Rectangle2D
/**
 * Created by gologuzov on 29.07.16.
 */
class WatermarkComponentBuilder extends ImageBuilder {
    public WatermarkComponentBuilder(String watermarkText, DRReportTemplate template) {
        int width = template.pageWidth - template.pageMargin.left - template.pageMargin.right
        int height = template.pageHeight - template.pageMargin.top - template.pageMargin.bottom
        setImage(new WatermarkExpression(watermarkText, width, height))
        setWidth(width)
        setHeight(height)
        setUsingCache(true)
    }

    private static class WatermarkExpression extends AbstractSimpleExpression<Renderable> {
        private Renderable renderer

        public WatermarkExpression(String watermarkText, int width, int height) {
            this.renderer = new WaterMarkRenderer(watermarkText, width, height)
        }

        @Override
        public Renderable evaluate(ReportParameters reportParameters) {
            return renderer
        }
    }

    private static class WaterMarkRenderer extends AbstractRenderer implements Graphics2DRenderable {
        private String watermarkText
        private Integer width
        private Integer height

        public WaterMarkRenderer(String watermarkText, int width, int height) {
            this.watermarkText = watermarkText
            this.width = width
            this.height = height
        }

        @Override
        void render(JasperReportsContext jasperReportsContext, Graphics2D g2, Rectangle2D rectangle) throws JRException {
            if (watermarkText != null && watermarkText.trim().length() > 0) {
                g2.setColor(Templates.watermarkGrey)
                DRFont drFont = Templates.getWatermarkFont().build()
                g2.setFont(new Font(drFont.fontName, Font.PLAIN, drFont.fontSize))
                double angle = -1 * Math.atan(rectangle.height/rectangle.width)
                TextUtilities.drawRotatedString(
                        watermarkText,
                        g2,
                        (float)rectangle.centerX,
                        (float)rectangle.centerY,
                        TextAnchor.CENTER,
                        angle,
                        (float)rectangle.centerX,
                        (float)rectangle.centerY)
            }
        }
    }
}
