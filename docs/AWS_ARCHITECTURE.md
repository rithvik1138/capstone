# AWS Infrastructure Deployment and Integration Guide

## 1. Architectural Overview on AWS

When expanding from a local/on-premise deployment to AWS, the system leverages managed services to ensure high availability, multi-AZ resilience, and zero data leakage between user devices.

```
                  +----------------------------------------------+
                  |               Amazon Route 53                |
                  +----------------------+-----------------------+
                                         |
                                         v
                  +----------------------------------------------+
                  |        AWS CloudFront (CDN + SSL/TLS)        |
                  +----------------------+-----------------------+
                                         |
                        +----------------+----------------+
                        |                                 |
                        v                                 v
             +--------------------+            +--------------------+
             |   S3 Bucket        |            |   Application Load |
             | (React Frontend)   |            |   Balancer (ALB)   |
             +--------------------+            +----------+---------+
                                                          |
                                                          v
                                               +--------------------+
                                               |  ECS Fargate / EC2 |
                                               | (Spring Boot App)  |
                                               +----+----------+----+
                                                    |          |
                           +------------------------+          +-----------------------+
                           |                                                           |
                           v                                                           v
              +-------------------------+                                 +-------------------------+
              |     Amazon RDS MySQL    |                                 |  Prometheus & Grafana   |
              |     (Multi-AZ Engine)   |                                 |  (EC2 / EKS Container)  |
              +-------------------------+                                 +-------------------------+
```

## 2. Monitored AWS Nodes (e.g. AWS EC2)

1. **Agent Installation on EC2**:
   - Monitored EC2 instances (Ubuntu / Amazon Linux 2023) can run the standalone monitoring agent.
   - User registers the EC2 node via Web UI selecting `AWS_EC2`.
   - The user runs the 1-line installation script inside the EC2 instance:
     ```bash
     curl -sSL https://api.yourcloudmonitor.com/install.sh | bash -s -- --token=<EC2_AGENT_TOKEN>
     ```
2. **Security Groups & VPC Networking**:
   - Inbound: Port 8080 (HTTPS) on Spring Boot ALB is open to agent outbound traffic.
   - Prometheus server in AWS VPC scrapes EC2 nodes via Private IP on port 9100/9200.

## 3. Production Environment Secrets Management

- **AWS Secrets Manager / AWS Systems Manager Parameter Store**:
  - `SPRING_DATASOURCE_PASSWORD`
  - `APP_JWT_SECRET`
  - `OMNIROUTE_API_KEY`
- Injected securely into ECS Task Definitions or EC2 environment variables without hardcoding.