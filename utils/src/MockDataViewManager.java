import lxx.targeting.tomcat_claws.data_analise.DataView;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;
import lxx.targeting.tomcat_claws.data_analise.SingleSourceDataView;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

/**
 * User: Aleksey Zhidkov
 * Date: 15.06.12
 */
public class MockDataViewManager extends DataViewManager {

    private final DataView dataView;

    public MockDataViewManager(Attribute[] attrs) {
        super(null, null);
        dataView = new SingleSourceDataView(attrs, new double[]{0, 1}, "Test", 5000);
    }

    public void addTurnSnapshot(TurnSnapshot ts) {
        dataView.addEntry(ts);
    }

    @Override
    public DataView[] getDuelDataViews() {
        return new DataView[]{dataView};
    }
}
