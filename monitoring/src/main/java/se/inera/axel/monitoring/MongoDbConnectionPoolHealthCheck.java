package se.inera.axel.monitoring;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeData;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheck extends JmxHealthCheck {
    // Used by Yaml
    private MongoDbConnectionPoolHealthCheck() {

    }

    public MongoDbConnectionPoolHealthCheck(String healthCheckId, String objectNamePattern, Map<String, String> expectedAttributes) {
        super(healthCheckId, objectNamePattern, expectedAttributes);
    }

    @Override
    protected void checkAdditional(List<HealthStatus> healthStatuses, Set<ObjectInstance> foundMBeans, MBeanServer mBeanServer) throws Exception {
        for (ObjectInstance foundMBean : foundMBeans) {
            Integer maxSize = (Integer) mBeanServer.getAttribute(foundMBean.getObjectName(), "MaxSize");
            Integer size = (Integer) mBeanServer.getAttribute(foundMBean.getObjectName(), "Size");
            if(size>=maxSize){
                healthStatuses.add(
                    new HealthStatus(
                            getHealthCheckId(),
                            SeverityLevel.WARNING,
                            String.format("All %d available connections to MongoDB in use.",maxSize),
                            foundMBean.getObjectName().getCanonicalName(),
                            1.0));
            }

        }
    }
}
