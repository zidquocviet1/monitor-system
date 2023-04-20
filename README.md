## Monitoring System in Spring Boot

List of technologies are used in this project:

1. Micrometer
2. Prometheus
3. Grafana
4. Resilience4j
5. Redis
6. Traefik
7. Docker
8. Alert Manager
9. OpenFeign
10. Spring Actuator
11. cAdvisor

# Collect Metrics

This project is used to track the api or computer usage in the operation process. I use Micrometer
to record all the metrics should be tracked by the administrator like: Gauge, Counter, Timer... After that we need the
help of Actuator publish Prometheus metrics endpoint in order to make the Prometheus service pull metrics which were record
and save them into its Time-Series Database.

You can view the metrics which were collect in the endpoint: <a href="http://localhost:8081/actuator/prometheus">Prometheus Metrics</a>

<img src="docs/prometheus-metrics-actuator.jpeg" alt="">

# Metrics Visualization

In this step, i will use the Grafana to get metrics from Prometheus database and then create a new dashboard with some
attribute like: Start Time, Uptime, CPU usage,  HTTP Request Rate, Duration, Docker Container CPU usage. To monitor
container CPU usage i used cAdvisor service that was created by Google to collect docker containers information. The sample
dashboard is json format in the [dashboards](./src/main/resources/grafana/provisioning/dashboards) folder. If you go to
[this](http://localhost:3000) endpoint then choose the `VMess Monitoring Dashboard` you will get the result like this:

<img src="docs/vmess-grafana-dashboard.jpeg" alt="">

* Notice: If it requires you to enter your username and password to login, you can use the hardcord username and password
in [this](./src/main/resources/grafana/config.monitoring) file with default username is `admin`. You can change your password in that folder if you want.

# Alert Notification

With Prometheus, it also supports the Alert Manager service to detect which job is down. In the downtime section, we can 
configure the time exceed to notify our slack system, to do that you can change the [alert.rules](./src/main/resources/prometheus/alert.rules) file
to set up with prometheus system.

````
groups:
- name: monitor-alert
  rules:

  # Alert for any instance that is unreachable for >2 minutes.
  - alert: service_down
    expr: up == 0
    for: 1m
    labels:
      severity: page
    annotations:
      summary: "Instance {{ $labels.instance }} down"
      description: "{{ $labels.instance }} of job {{ $labels.job }} has been down for more than 2 minutes."

  - alert: high_load
    expr: node_load1 > 0.5
    for: 2m
    labels:
      severity: page
    annotations:
      summary: "Instance {{ $labels.instance }} under high load"
      description: "{{ $labels.instance }} of job {{ $labels.job }} is under high load."
````

In the example rules above, I delegated to Prometheus that notify to alert manager system whenever the job is down for 
over 1 minute or the cpu usage is over 50%.

Then the alert manager system will handle and send to our Slack channel with the config message format in [config](./src/main/resources/alertmanager/config.yml) file.

````
global:
  resolve_timeout: 1m
  slack_api_url: 'https://hooks.slack.com/services/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'

route:
  receiver: 'slack-notifications'

receivers:
  - name: 'slack-notifications'
    slack_configs:
      - channel: '#development'
        send_resolved: true
        icon_url: https://avatars3.githubusercontent.com/u/3380462
        title: |-
          [{{ .Status | toUpper }}{{ if eq .Status "firing" }}:{{ .Alerts.Firing | len }}{{ end }}] {{ .CommonLabels.alertname }} for {{ .CommonLabels.job }}
          {{- if gt (len .CommonLabels) (len .GroupLabels) -}}
            {{" "}}(
            {{- with .CommonLabels.Remove .GroupLabels.Names }}
              {{- range $index, $label := .SortedPairs -}}
                {{ if $index }}, {{ end }}
                {{- $label.Name }}="{{ $label.Value -}}"
              {{- end }}
            {{- end -}}
            )
          {{- end }}
        text: >-
          {{ range .Alerts -}}
          *Alert:* {{ .Annotations.title }}{{ if .Labels.severity }} - `{{ .Labels.severity }}`{{ end }}
          
          *Description:* {{ .Annotations.description }}
          
          *Details:*
            {{ range .Labels.SortedPairs }} • *{{ .Name }}:* `{{ .Value }}`
            {{ end }}
          {{ end }}
````

* Notice: Replace your `slack_api_url` to your Slack channel hook api endpoint, unless it will not work properly.

# Running

To run the application, I want you to make sure the Redis service is running on your machine or docker container.

If you want to run the application on the development section, just run the application with profiles `dev`. Otherwise, you
can run the whole application through docker compose. After that you can see the result.

# Manual Testing

After the application is run, you can access to [this](http://localhost:8081/v1/accounts/1) endpoint to verify 
the Distributed Rate Limiter is working or not. As I notice, the Resilience4j is integrated to use as a fault tolerance system. 
These are the built-in technologies that were used: Circuit Breaker, Retry, Bulkhead.

To validate the rate limit of user request, I have created the Lua Script to work with Redis service. The example of script
like this:

````
local key = KEYS[1]
local window = tonumber(ARGV[1])
local max_request_count = tonumber(ARGV[2])
local amount = tonumber(ARGV[3])

local current_time = redis.call('time')
local trim_time = tonumber(current_time[1])

redis.call('ZREMRANGEBYSCORE', key, 0, trim_time - window)
local requests = redis.call('ZCARD', key)

if requests + amount <= max_request_count then
    redis.call('ZADD', key, current_time[1], current_time[1] .. current_time[2])
    redis.call('EXPIRE', key, window)
    return false
end
return true
````

With the powerful of Lua script, I used `lettuce` library to communicate with Redis through Java client and associate with
fault tolerance to monitor Redis command and then decided to retry the running command or not. After running this script,
the result will return boolean type to indicate that request is exceed maximum permitted request or not.

If not the response with status code is 429 and response header include `X-Retry-After` to notify the client that should 
retry after this time in seconds.

With bulkhead technology, I use OpenFeign to communicate with Spotify service through webAPI to get list of track metadata.
The configuration is set up with max 2 concurrent request in the time interval to avoid burst bandwidth in our system and
also Spotify service.