# Monitoring and Alerting Setup Guide

## Overview

This document provides comprehensive guidance for setting up monitoring and alerting infrastructure for the IT Infrastructure Asset Management System in production.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Spring Boot Application                             │   │
│  │  - Actuator Endpoints                                │   │
│  │  - Micrometer Metrics                                │   │
│  │  - Structured Logging                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Metrics Collection                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Prometheus                                          │   │
│  │  - Scrapes /actuator/prometheus endpoint            │   │
│  │  - Stores time-series metrics                       │   │
│  │  - Retention: 15 days                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Visualization Layer                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Grafana                                             │   │
│  │  - Dashboards for metrics visualization             │   │
│  │  - Alert management                                  │   │
│  │  - User access control                               │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Alerting Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Alertmanager                                        │   │
│  │  - Routes alerts to appropriate channels             │   │
│  │  - Email, Slack, PagerDuty integration               │   │
│  │  - Alert deduplication and grouping                  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Log Aggregation                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ELK Stack (Elasticsearch, Logstash, Kibana)        │   │
│  │  - Centralized log storage                           │   │
│  │  - Log search and analysis                           │   │
│  │  - Log-based alerting                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Application Metrics Configuration

### Spring Boot Actuator Setup

**File**: `backend/src/main/resources/application-prod.properties`

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true

# Metrics Export
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=production

# Custom Metrics
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
management.metrics.enable.tomcat=true
management.metrics.enable.logback=true
```

### Custom Business Metrics

**File**: `backend/src/main/java/com/company/assetmanagement/config/MetricsConfig.java`

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "it-asset-management")
            .commonTags("environment", "production");
    }
    
    @Bean
    public Counter userLoginCounter(MeterRegistry registry) {
        return Counter.builder("user.login.total")
            .description("Total number of user login attempts")
            .tag("status", "success")
            .register(registry);
    }
    
    @Bean
    public Counter assetCreationCounter(MeterRegistry registry) {
        return Counter.builder("asset.creation.total")
            .description("Total number of assets created")
            .register(registry);
    }
    
    @Bean
    public Gauge activeSessionsGauge(MeterRegistry registry, SessionRepository sessionRepository) {
        return Gauge.builder("session.active.count", sessionRepository, 
            repo -> repo.countByIsActiveTrue())
            .description("Number of active user sessions")
            .register(registry);
    }
}
```

### Key Metrics to Track

| Metric | Type | Description | Alert Threshold |
|--------|------|-------------|-----------------|
| `http_server_requests_seconds` | Timer | HTTP request duration | p95 > 1s |
| `jvm_memory_used_bytes` | Gauge | JVM memory usage | > 80% of max |
| `jvm_gc_pause_seconds` | Timer | GC pause duration | p95 > 100ms |
| `hikaricp_connections_active` | Gauge | Active DB connections | > 18 (90% of pool) |
| `user_login_total` | Counter | Login attempts | Sudden spike |
| `asset_creation_total` | Counter | Assets created | Unusual patterns |
| `session_active_count` | Gauge | Active sessions | > 100 |
| `system_cpu_usage` | Gauge | CPU usage | > 80% |

---

## 2. Prometheus Configuration

### Installation

```bash
# Download Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.45.0/prometheus-2.45.0.linux-amd64.tar.gz
tar xvfz prometheus-2.45.0.linux-amd64.tar.gz
cd prometheus-2.45.0.linux-amd64
```

### Configuration File

**File**: `prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'production'
    environment: 'prod'

# Alertmanager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - 'localhost:9093'

# Load rules once and periodically evaluate them
rule_files:
  - 'alert_rules.yml'

# Scrape configurations
scrape_configs:
  # IT Asset Management Application
  - job_name: 'it-asset-management'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app-server-1:8080', 'app-server-2:8080']
        labels:
          instance: 'app-server'
    
  # Database Monitoring
  - job_name: 'sqlserver'
    static_configs:
      - targets: ['db-server:9399']
        labels:
          instance: 'database'
    
  # Node Exporter (System Metrics)
  - job_name: 'node'
    static_configs:
      - targets: ['app-server-1:9100', 'app-server-2:9100', 'db-server:9100']
```

### Alert Rules

**File**: `alert_rules.yml`

```yaml
groups:
  - name: application_alerts
    interval: 30s
    rules:
      # High Error Rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
          component: application
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }} (threshold: 5%)"
      
      # Slow Response Time
      - alert: SlowResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
          component: application
        annotations:
          summary: "Slow response time detected"
          description: "95th percentile response time is {{ $value }}s (threshold: 1s)"
      
      # High Memory Usage
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.8
        for: 5m
        labels:
          severity: warning
          component: jvm
        annotations:
          summary: "High JVM memory usage"
          description: "JVM heap usage is {{ $value | humanizePercentage }} (threshold: 80%)"
      
      # Database Connection Pool Exhaustion
      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
          component: database
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Connection pool usage is {{ $value | humanizePercentage }} (threshold: 90%)"
      
      # Application Down
      - alert: ApplicationDown
        expr: up{job="it-asset-management"} == 0
        for: 1m
        labels:
          severity: critical
          component: application
        annotations:
          summary: "Application is down"
          description: "Application {{ $labels.instance }} is not responding"
      
      # High CPU Usage
      - alert: HighCPUUsage
        expr: system_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
          component: system
        annotations:
          summary: "High CPU usage detected"
          description: "CPU usage is {{ $value | humanizePercentage }} (threshold: 80%)"
      
      # Failed Login Spike
      - alert: FailedLoginSpike
        expr: rate(user_login_total{status="failed"}[5m]) > 10
        for: 2m
        labels:
          severity: warning
          component: security
        annotations:
          summary: "Spike in failed login attempts"
          description: "Failed login rate is {{ $value }} per second (threshold: 10/s)"
      
      # Database Slow Queries
      - alert: DatabaseSlowQueries
        expr: rate(spring_data_repository_invocations_seconds_sum[5m]) / rate(spring_data_repository_invocations_seconds_count[5m]) > 0.5
        for: 5m
        labels:
          severity: warning
          component: database
        annotations:
          summary: "Slow database queries detected"
          description: "Average query time is {{ $value }}s (threshold: 0.5s)"
```

### Starting Prometheus

```bash
./prometheus --config.file=prometheus.yml --storage.tsdb.retention.time=15d
```

---

## 3. Grafana Dashboard Setup

### Installation

```bash
# Download Grafana
wget https://dl.grafana.com/oss/release/grafana-10.0.0.linux-amd64.tar.gz
tar -zxvf grafana-10.0.0.linux-amd64.tar.gz
cd grafana-10.0.0
```

### Configuration

**File**: `conf/defaults.ini`

```ini
[server]
http_port = 3000
domain = monitoring.example.com
root_url = https://monitoring.example.com

[security]
admin_user = admin
admin_password = ${GRAFANA_ADMIN_PASSWORD}

[auth]
disable_login_form = false
oauth_auto_login = false

[datasources]
# Prometheus datasource will be added via provisioning
```

### Datasource Provisioning

**File**: `conf/provisioning/datasources/prometheus.yml`

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://localhost:9090
    isDefault: true
    editable: false
```

### Dashboard JSON Templates

#### Application Performance Dashboard

**Key Panels**:
1. **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
2. **Response Time (p95)**: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
3. **Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
4. **Active Sessions**: `session_active_count`
5. **JVM Memory Usage**: `jvm_memory_used_bytes{area="heap"}`
6. **GC Pause Time**: `rate(jvm_gc_pause_seconds_sum[5m])`

#### Database Performance Dashboard

**Key Panels**:
1. **Connection Pool Usage**: `hikaricp_connections_active / hikaricp_connections_max`
2. **Query Execution Time**: `rate(spring_data_repository_invocations_seconds_sum[5m])`
3. **Database Connections**: `hikaricp_connections_active`
4. **Connection Wait Time**: `hikaricp_connections_acquire_seconds`

#### Business Metrics Dashboard

**Key Panels**:
1. **User Logins**: `rate(user_login_total[5m])`
2. **Assets Created**: `rate(asset_creation_total[1h])`
3. **Active Users**: `session_active_count`
4. **Failed Login Attempts**: `rate(user_login_total{status="failed"}[5m])`

### Starting Grafana

```bash
./bin/grafana-server web
```

Access Grafana at: `http://localhost:3000`

---

## 4. Alertmanager Configuration

### Installation

```bash
wget https://github.com/prometheus/alertmanager/releases/download/v0.26.0/alertmanager-0.26.0.linux-amd64.tar.gz
tar xvfz alertmanager-0.26.0.linux-amd64.tar.gz
cd alertmanager-0.26.0.linux-amd64
```

### Configuration File

**File**: `alertmanager.yml`

```yaml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alerts@example.com'
  smtp_auth_username: 'alerts@example.com'
  smtp_auth_password: '${SMTP_PASSWORD}'

# Route tree
route:
  receiver: 'default-receiver'
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  
  routes:
    # Critical alerts go to PagerDuty and email
    - match:
        severity: critical
      receiver: 'critical-alerts'
      continue: true
    
    # Warning alerts go to Slack and email
    - match:
        severity: warning
      receiver: 'warning-alerts'
    
    # Security alerts go to security team
    - match:
        component: security
      receiver: 'security-team'

# Receivers
receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'ops-team@example.com'
        headers:
          Subject: '[IT Asset Management] Alert: {{ .GroupLabels.alertname }}'
  
  - name: 'critical-alerts'
    pagerduty_configs:
      - service_key: '${PAGERDUTY_SERVICE_KEY}'
        description: '{{ .GroupLabels.alertname }}: {{ .CommonAnnotations.summary }}'
    email_configs:
      - to: 'ops-team@example.com,management@example.com'
        headers:
          Subject: '[CRITICAL] IT Asset Management Alert'
    slack_configs:
      - api_url: '${SLACK_WEBHOOK_URL}'
        channel: '#alerts-critical'
        title: 'Critical Alert: {{ .GroupLabels.alertname }}'
        text: '{{ .CommonAnnotations.description }}'
  
  - name: 'warning-alerts'
    email_configs:
      - to: 'ops-team@example.com'
    slack_configs:
      - api_url: '${SLACK_WEBHOOK_URL}'
        channel: '#alerts-warning'
        title: 'Warning: {{ .GroupLabels.alertname }}'
        text: '{{ .CommonAnnotations.description }}'
  
  - name: 'security-team'
    email_configs:
      - to: 'security-team@example.com'
        headers:
          Subject: '[SECURITY] IT Asset Management Alert'

# Inhibition rules
inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'instance']
```

### Starting Alertmanager

```bash
./alertmanager --config.file=alertmanager.yml
```

---

## 5. Log Aggregation with ELK Stack

### Logback Configuration

**File**: `backend/src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Console Appender with JSON format -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"it-asset-management","environment":"production"}</customFields>
        </encoder>
    </appender>
    
    <!-- File Appender with JSON format -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/it-asset-management/application.log</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"it-asset-management","environment":"production"}</customFields>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/it-asset-management/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- Logstash Appender (TCP) -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash-server:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"it-asset-management","environment":"production"}</customFields>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
    
    <logger name="com.company.assetmanagement" level="DEBUG"/>
    <logger name="org.springframework.security" level="INFO"/>
    <logger name="org.hibernate.SQL" level="INFO"/>
</configuration>
```

### Logstash Configuration

**File**: `logstash.conf`

```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  # Parse timestamp
  date {
    match => ["timestamp", "ISO8601"]
    target => "@timestamp"
  }
  
  # Add geoip for IP addresses
  if [clientIp] {
    geoip {
      source => "clientIp"
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "it-asset-management-%{+YYYY.MM.dd}"
  }
}
```

### Kibana Dashboard Setup

**Key Visualizations**:
1. **Error Rate Over Time**: Line chart of log level ERROR
2. **Top Error Messages**: Table of most frequent errors
3. **User Activity**: Bar chart of user actions
4. **Failed Login Attempts**: Map of failed login locations
5. **API Endpoint Usage**: Pie chart of endpoint calls

---

## 6. Database Monitoring

### SQL Server Monitoring Queries

```sql
-- Active Connections
SELECT 
    DB_NAME(dbid) as DatabaseName,
    COUNT(dbid) as NumberOfConnections,
    loginame as LoginName
FROM sys.sysprocesses
WHERE dbid > 0
GROUP BY dbid, loginame;

-- Long Running Queries
SELECT 
    r.session_id,
    r.start_time,
    r.status,
    r.command,
    SUBSTRING(t.text, (r.statement_start_offset/2)+1,
        ((CASE r.statement_end_offset
            WHEN -1 THEN DATALENGTH(t.text)
            ELSE r.statement_end_offset
        END - r.statement_start_offset)/2)+1) AS query_text,
    r.wait_type,
    r.wait_time,
    r.cpu_time,
    r.total_elapsed_time
FROM sys.dm_exec_requests r
CROSS APPLY sys.dm_exec_sql_text(r.sql_handle) t
WHERE r.total_elapsed_time > 5000 -- queries running > 5 seconds
ORDER BY r.total_elapsed_time DESC;

-- Database Size
SELECT 
    DB_NAME() AS DatabaseName,
    SUM(size * 8 / 1024) AS SizeMB
FROM sys.database_files;

-- Index Fragmentation
SELECT 
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
INNER JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE ips.avg_fragmentation_in_percent > 10
AND i.name IS NOT NULL
ORDER BY ips.avg_fragmentation_in_percent DESC;
```

### SQL Server Exporter for Prometheus

```bash
# Download SQL Server Exporter
wget https://github.com/awaragi/prometheus-mssql-exporter/releases/download/v0.4.0/prometheus-mssql-exporter-linux-amd64.tar.gz
tar xvfz prometheus-mssql-exporter-linux-amd64.tar.gz

# Configuration
export SQLSERVER_CONNECTION_STRING="Server=localhost;Database=ITAssetManagement;User Id=monitoring;Password=MonitoringPass123;"
./prometheus-mssql-exporter
```

---

## 7. Health Checks

### Application Health Endpoints

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "SQL Server")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}
```

### Kubernetes Liveness and Readiness Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

---

## 8. Alert Response Procedures

### Critical Alert Response

1. **Acknowledge Alert**: Acknowledge in PagerDuty/Alertmanager
2. **Assess Impact**: Check Grafana dashboards for scope
3. **Check Logs**: Review recent logs in Kibana
4. **Escalate if Needed**: Contact senior engineer if unresolved in 15 minutes
5. **Communicate**: Update status page and notify stakeholders
6. **Resolve**: Fix issue and verify metrics return to normal
7. **Document**: Create incident report

### Warning Alert Response

1. **Review Alert**: Check Grafana for context
2. **Investigate**: Review logs and metrics
3. **Create Ticket**: If action needed, create Jira ticket
4. **Monitor**: Continue monitoring for escalation
5. **Resolve**: Address during business hours if non-urgent

---

## 9. Maintenance Tasks

### Daily

- [ ] Review error logs in Kibana
- [ ] Check Grafana dashboards for anomalies
- [ ] Verify backup completion
- [ ] Review active alerts

### Weekly

- [ ] Review performance trends
- [ ] Check disk space usage
- [ ] Review security logs
- [ ] Update alert thresholds if needed

### Monthly

- [ ] Review and optimize alert rules
- [ ] Update Grafana dashboards
- [ ] Conduct alert drill
- [ ] Review monitoring costs

---

## 10. Troubleshooting

### Prometheus Not Scraping Metrics

**Symptoms**: No data in Grafana, Prometheus targets down

**Resolution**:
1. Check application is running: `curl http://app-server:8080/actuator/health`
2. Verify metrics endpoint: `curl http://app-server:8080/actuator/prometheus`
3. Check Prometheus configuration: `./promtool check config prometheus.yml`
4. Review Prometheus logs: `tail -f /var/log/prometheus/prometheus.log`

### High Memory Alerts

**Symptoms**: JVM memory usage > 80%

**Resolution**:
1. Check for memory leaks: Review heap dump
2. Increase JVM heap size: `-Xmx2g`
3. Optimize queries: Review slow database queries
4. Scale horizontally: Add more application instances

### Database Connection Pool Exhaustion

**Symptoms**: Connection timeout errors, slow response times

**Resolution**:
1. Check active connections: Review HikariCP metrics
2. Identify long-running queries: Use SQL Server DMVs
3. Increase pool size: `spring.datasource.hikari.maximum-pool-size=30`
4. Optimize queries: Add indexes, refactor N+1 queries

---

## Appendix: Useful Commands

### Prometheus

```bash
# Check configuration
./promtool check config prometheus.yml

# Check alert rules
./promtool check rules alert_rules.yml

# Query metrics
curl 'http://localhost:9090/api/v1/query?query=up'
```

### Grafana

```bash
# Reset admin password
./bin/grafana-cli admin reset-admin-password newpassword

# Install plugins
./bin/grafana-cli plugins install grafana-piechart-panel
```

### Elasticsearch

```bash
# Check cluster health
curl -X GET "localhost:9200/_cluster/health?pretty"

# List indices
curl -X GET "localhost:9200/_cat/indices?v"

# Delete old indices
curl -X DELETE "localhost:9200/it-asset-management-2024.01.01"
```

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Owner**: DevOps Team
