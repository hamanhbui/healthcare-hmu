/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wearsensorlogging;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
/**
 *
 * @author habui
 */
public class JFreeSensorGraph extends ApplicationFrame{
    private final float MINMAX;
    private final int COUNT = 2 * 60;
    private final int FAST = 30;
    private final String title;
    private Timer timer;
    private JFreeSensorData sensorData;
    public JFreeSensorGraph(final String title, final float MINMAX, JFreeSensorData data) {
        super(title);
        this.title=title;
        this.MINMAX=MINMAX;
        sensorData=data;
             
        final DynamicTimeSeriesCollection dataset =
            new DynamicTimeSeriesCollection(3, COUNT, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
        dataset.addSeries(initData(), 0, "X");
        dataset.addSeries(initData(), 1, "Y");
        dataset.addSeries(initData(), 2, "Z");
        JFreeChart chart = createChart(dataset);

        this.add(new ChartPanel(chart), BorderLayout.CENTER);

        timer = new Timer(FAST, new ActionListener() {

            float[] newData = new float[3];

            @Override
            public void actionPerformed(ActionEvent e) {
                newData[0] = sensorData.x;
                newData[1] = sensorData.y;
                newData[2] = sensorData.z;
                dataset.advanceTime();
                dataset.appendData(newData);
            }
        });
    }
    

    private float[] initData() {
        float[] a = new float[COUNT];
        for (int i = 0; i < a.length; i++) {
            a[i] = 0;
        }
        return a;
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            title, "hh:mm:ss", "units", dataset, true, true, false);
        final XYPlot plot = result.getXYPlot();
        ValueAxis domain = plot.getDomainAxis();
        ValueAxis range = plot.getRangeAxis();
        range.setRange(-MINMAX, MINMAX);
        return result;
    }

    public void start() {
        timer.start();
    }
}
