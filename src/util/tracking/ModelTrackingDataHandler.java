package util.tracking;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import controller.TrackingDataHandler;
import facade.modeling.FModel;
import view.FModelView;
import view.ModelTrackingComponent;
import view.Tracker;

public class ModelTrackingDataHandler implements TrackingDataHandler
{
    private ModelTrackingComponent trackingComponent;
    private FModelView modelView;

    public ModelTrackingDataHandler(FModelView modelView)
    {
        this.modelView = modelView;
    }

    public void registerTrackingComponent(ModelTrackingComponent trackingComponent)
    {
        this.trackingComponent = trackingComponent;
    }

    public String getHTMLTrackingLog()
    {
        return trackingComponent.getHTMLString();
    }

    public String[] getEncodedCSVExport()
    {
        FModel model = modelView.getSelectedModel();

        StringBuilder legend = new StringBuilder();

        legend.append("<HTML><BODY><B>Legend - ").append(model.getName()).append("</B>");
        Tracker tracker = trackingComponent.findTrackerFor(model).get();
        TrackerDataStore dataStore = tracker.getDataStorage();
        EncodedTrackerDataStore encodedDataStore = new EncodedTrackerDataStore(dataStore);

        for (TrackerDataStore.TrackedVariableMetadata meta : dataStore.getHeader())
        {
            legend.append("<P><B>").append(meta.name).append("</B>");
            legend.append("<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"1\" ");
            legend.append("bordercolorlight=\"#C0C0C0\" bordercolordark=\"#C0C0C0\" bordercolor=\"#C0C0C0\" >");
            legend.append("<TR><TD width=\"100\"><B>Key</B></TD>");
            legend.append("<TD width=\"100\"><B>Value</B></TD></TR>");

            encodedDataStore.forEachLegend(meta.name, meta.type, (String key, Integer value) -> {
                legend.append("<TR><TD nowrap>").append(key).append("</TD>");
                legend.append("<TD nowrap>").append(value).append("</TD></TR>");
            });
            legend.append("</TABLE>");
        }

        legend.append("</BODY></HTML>");

        String[] val = new String[2];
        val[0] = legend.toString();
        val[1] = createCSVString(encodedDataStore);
        return val;
    }

    public String getCSVExport()
    {
        Optional<Tracker> tracker = trackingComponent.findTrackerFor(modelView.getSelectedModel());
        return createCSVString(tracker.get().getDataStorage());
    }

    private String createCSVString(TrackerDataStore storage)
    {
        String delim = ",";
        String newLine = "\n";
        StringBuilder buffer = new StringBuilder();

        List<?> headerRow = trackingComponent.getHeaderRow();

        Iterable<TrackerDataStore.TrackedVariableMetadata> headers = storage.getHeader();
        buffer.append(delim);
        buffer.append(String.join(delim, TrackerDataStore.StringIterable(headers)));
        buffer.append(newLine);

        ListIterator<?> it = headerRow.listIterator();
        while (it.hasNext())
        {
            int i = it.nextIndex();
            buffer.append(it.next());
            buffer.append(delim);

            for (TrackerDataStore.TrackedVariableMetadata meta : headers)
            {
                if (i >= storage.dataSize(meta))
                {
                    break;
                }
                Object obj = storage.getData(meta, i);
                buffer.append((obj == null) ? "" : obj.toString());
                buffer.append(delim);
            }
            if (buffer.charAt(buffer.length() - 1) == ',')
            {
                buffer.deleteCharAt(buffer.length() - 1);
            }

            buffer.append(newLine);
        }

        if (buffer.charAt(buffer.length() - 1) == '\n')
        {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        return buffer.toString();
    }
}
