package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.imagej.ImgPlus;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;

import clearvolume.ClearVolume;
import clearvolume.renderer.ClearVolumeRendererInterface;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView, ActionListener {

    private class ClearVolumeThread implements Runnable {

        private ClearVolumeRendererInterface cv;
        private Container container;

        /**
         * @param ctnrClearVolume
         */
        public ClearVolumeThread(final Container ctnrClearVolume) {
            this.container = ctnrClearVolume;
        }

        @Override
        public void run() {
            cv = ClearVolume.initRealImg(imgPlus, "ClearVolume TableCellView",
                                            512, 512,
                                            textureWidth, textureHeight,
                                            true,
                                            minIntensity, maxIntensity);
            cv.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
            try{
                cv.requestDisplay();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public void dispose() {
            if (cv != null) {
                cv.setVisible(false);
                cv.close();
            }
        }

        public void updateView() {
            activateGuiValues();

            container.remove(cv.getNewtCanvasAWT());
            run();
            cv.resetRotationTranslation();
            cv.resetBrightnessAndGammaAndTransferFunctionRanges();

            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    container.add(cv.getNewtCanvasAWT());
//                    cv.setVisible(true);
                }

            });
        }

    }

    private ImgPlus<T> imgPlus;

    private JPanel mainPanel;
    private Container ctnrClearVolume;
    private JPanel panelControls;
    private JButton buttonUpdateView;
    private JTextField txtTextureWidth;

    private JTextField txtTextureHeight;

    private JTextField txtMinInt;

    private JTextField txtMaxInt;

    private JTextField txtVoxelSizeX;

    private JTextField txtVoxelSizeY;

    private JTextField txtVoxelSizeZ;

    private int textureWidth;

    private int textureHeight;

    private double minIntensity;

    private double maxIntensity;

    private double voxelSizeZ;

    private double voxelSizeY;

    private double voxelSizeX;

    private ClearVolumeThread cvThread;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "ClearVolume-KNIME integration.\n" + "The viewer is capable of CUDA rendering of ImgPlus images. "
                + "If CUDA is not available a OpenCL render will be used.\n"
                + "For more information see 'https://bitbucket.org/clearvolume/clearvolume/wiki/Home'.\n"
                + "Credits to: Loic Royer, Martin Weigert, Ulrik Guenther, and Florian Jug.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "ClearVolume";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getViewComponent() {
        setDefaultValues();
        buildGui();
        updateGuiFieldValues();
        return mainPanel;
    }

    private void setDefaultValues() {
        this.textureWidth = 1024;
        this.textureHeight = 1024;
        this.minIntensity = 0.;
        this.maxIntensity = 255;
        this.voxelSizeX = 1.;
        this.voxelSizeY = 1.;
        this.voxelSizeZ = 1.;
    }

    private void buildGui() {

        mainPanel = new JPanel(new BorderLayout(3,3));

        ctnrClearVolume = new Container();
        ctnrClearVolume.setLayout( new BorderLayout() );

        buttonUpdateView = new JButton("Update View");
        buttonUpdateView.addActionListener(this);

        panelControls = new JPanel(new BorderLayout());
        JPanel panelControlsHelper = new JPanel(new GridLayout(9,2));

        JLabel lblTextureWidth = new JLabel("Texture width");
        txtTextureWidth = new JTextField();
        JLabel lblTextureHeight = new JLabel("Texture height");
        txtTextureHeight = new JTextField();

        JLabel lblMinInt = new JLabel("Min. intensity");
        txtMinInt = new JTextField();
        JLabel lblMaxInt = new JLabel("Max. intensity");
        txtMaxInt = new JTextField();

        JLabel lblVoxelSizeX = new JLabel("VoxelDimension.X");
        txtVoxelSizeX = new JTextField();
        JLabel lblVoxelSizeY = new JLabel("VoxelDimension.Y");
        txtVoxelSizeY = new JTextField();
        JLabel lblVoxelSizeZ = new JLabel("VoxelDimension.Z");
        txtVoxelSizeZ = new JTextField();

        panelControlsHelper.add(lblTextureWidth);
        panelControlsHelper.add(txtTextureWidth);
        panelControlsHelper.add(lblTextureHeight);
        panelControlsHelper.add(txtTextureHeight);

        panelControlsHelper.add(lblMinInt);
        panelControlsHelper.add(txtMinInt);
        panelControlsHelper.add(lblMaxInt);
        panelControlsHelper.add(txtMaxInt);

        panelControlsHelper.add(lblVoxelSizeX);
        panelControlsHelper.add(txtVoxelSizeX);
        panelControlsHelper.add(lblVoxelSizeY);
        panelControlsHelper.add(txtVoxelSizeY);
        panelControlsHelper.add(lblVoxelSizeZ);
        panelControlsHelper.add(txtVoxelSizeZ);

        // PUTTING ALL TOGETHER

        panelControls.add(new JScrollPane(panelControlsHelper), BorderLayout.NORTH);
        panelControls.add(buttonUpdateView,BorderLayout.SOUTH);
        mainPanel.add(ctnrClearVolume, BorderLayout.CENTER);
        mainPanel.add(panelControls, BorderLayout.EAST);
    }

    public void updateGuiFieldValues() {
        txtTextureWidth.setText(""+this.textureWidth);
        txtTextureHeight.setText(""+this.textureHeight);

        txtMinInt.setText("" + this.minIntensity);
        txtMaxInt.setText("" + this.maxIntensity);

        txtVoxelSizeX.setText("" + this.voxelSizeX);
        txtVoxelSizeY.setText("" + this.voxelSizeY);
        txtVoxelSizeZ.setText("" + this.voxelSizeZ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConfigurationFrom(final ConfigRO config) {
        // forget it for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
        if (cvThread != null) {
            cvThread.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        if (cvThread != null) {
            cvThread.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfigurationTo(final ConfigWO config) {
        // no worries
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final DataValue valueToView) {
        imgPlus = ((ImgPlusValue<T>)valueToView).getImgPlus();
        setMetadataValues();
        updateGuiFieldValues();
        updateClearVolumeContainer();
    }

    /**
     *
     */
    private void updateClearVolumeContainer() {
        cvThread = new ClearVolumeThread(this.ctnrClearVolume);
        cvThread.run();
        ctnrClearVolume.add( cvThread.cv.getNewtCanvasAWT(), BorderLayout.CENTER );
    }

    /**
     * Uses the metadata in the ImgPlus to set voxel dimension and intensity range.
     */
    public void setMetadataValues() {
        if (imgPlus != null) {
            this.voxelSizeX = imgPlus.averageScale(0);
            this.voxelSizeY = imgPlus.averageScale(1);
            this.voxelSizeZ = imgPlus.averageScale(2);

            T min = imgPlus.firstElement().createVariable();
            T max = imgPlus.firstElement().createVariable();
            ComputeMinMax.computeMinMax(imgPlus, min, max);
            this.minIntensity = min.getRealDouble();
            this.maxIntensity = max.getRealDouble();
        }
    }

    /**
     * Read all validly entered text field values and activate them.
     */
    private void activateGuiValues() {
        int i;
        double d;

        try { i = Integer.parseInt( txtTextureWidth.getText() );
        }catch(NumberFormatException e) { i = this.textureWidth; }
        this.textureWidth = i;

        try {
            i = Integer.parseInt(txtTextureHeight.getText());
        } catch (NumberFormatException e) {
            i = this.textureHeight;
        }
        this.textureHeight = i;

        try {
            d = Double.parseDouble(txtMinInt.getText());
        } catch (NumberFormatException e) {
            d = this.minIntensity;
        }
        this.minIntensity = d;

        try {
            d = Double.parseDouble(txtMaxInt.getText());
        } catch (NumberFormatException e) {
            d = this.maxIntensity;
        }
        this.maxIntensity = d;

        try {
            d = Double.parseDouble(txtVoxelSizeX.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeX;
        }
        this.voxelSizeX = d;

        try {
            d = Double.parseDouble(txtVoxelSizeY.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeY;
        }
        this.voxelSizeY = d;

        try {
            d = Double.parseDouble(txtVoxelSizeZ.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeZ;
        }
        this.voxelSizeZ = d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if ( e.getSource().equals(buttonUpdateView) || e.getSource().equals(buttonUpdateView)) {
            cvThread.updateView();
        }
    }

}
