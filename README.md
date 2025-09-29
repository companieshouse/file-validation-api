# File Validation API

## Overview

This api is a Spring Boot microservice used to validate CSV files received from AML supervisory bodies. A nightly cron job (executed by this api) checks for validated and tagged files in the file-transfer-service’s location.
These files are then moved to an S3 bucket where they are consumed for downstream processing by the CHIPS system.

Please see below for documentation for further information related to this service and a high level overview of where this service fits within the AML ingestion lifecycle.


- [File Validation Common Component Documentation](https://companieshouse.atlassian.net/wiki/spaces/V1ACSP/pages/4913365008/File+Validation+Common+Component)
- [Import AML Data Service DevOps Handover](https://companieshouse.atlassian.net/wiki/spaces/V1ACSP/pages/5422579889/Import+AML+Data+Service+DevOps+Handover+acsp-aml-ingestion-terraform)

## CSV File Validation

The service performs comprehensive validation on uploaded CSV files to ensure they meet the required data quality standards before processing.

### File Structure Requirements

- **File Format**: Only CSV files are accepted
- **Column Count**: Must contain exactly 13 columns
- **Headers Required**: File must contain valid headers as the first row
- **Data Required**: Must contain at least one data row after headers

### Validation Error Handling

The validation process will reject files with:

- Incorrect file format (non-CSV files)
- Missing or incorrect headers
- Wrong number of columns
- Empty files or files with only headers
- Field length violations (exceeding maximum character limits)
- Invalid date format for Date of Birth field
- Corrupt or unparseable CSV structure

All validation errors logs include specific line numbers and detailed error messages to help diagnose validation issues.

## Development Requirements
In order to build and run the service locally you will need:

- Java 21
- Maven
- Git

## Developer Testing with Docker
If you have not already set up your Docker CHS Development environment, follow the steps in the Docker CHS Development repository README.

Then:

1. Open a new terminal and change to the root directory of "Docker CHS Development".
   ```bash
   chs-dev modules enable file-validation
   chs-dev services enable file-validation-api
   chs-dev development enable file-validation-api
   ```
2. Run docker using `chs-dev up` in the docker-chs-development directory, or `docker_chs up` from any directory.

To view logs:
- Use Docker Desktop dashboard.
- Run `docker_chs logs -f file-validation-api` to view logs.

Test the health check endpoint when running locally:
`http://api.chs.local:4001/file-validation-api/healthcheck`

Use Postman to test this endpoint.
A valid bearer token in the Authorization header is required.

## Environmental Variables
| Name | Description |
|------|-------------|
| MONGODB_URL | MongoDB connection URL |
| MONGODB_DATABASE | MongoDB database name |
| CHS_INTERNAL_API_KEY | Internal API Key for CHS authorization |
| FILE_TRANSFER_API_URL | URL of the external file transfer API |
| FILE_VALIDATION_MAX_FILE_SIZE | Maximum allowed file size |
| S3_BUCKET_NAME | Name of the S3 bucket for file storage |
| AWS_REGION | AWS region for S3 |
| VALIDATION_SCHEDULER_CRON | Cron expression for validation scheduler |
| VALIDATION_SCHEDULER_LOCK_AT_LIST_FOR | Scheduler lock duration minimum |
| VALIDATION_SCHEDULER_LOCK_AT_MOST_FOR | Scheduler lock duration maximum |
| VALIDATION_SCHEDULER_DEFAULT_LOCK_AT_MOST_FOR | Default scheduler lock duration |

## API Routes
The API provides the following endpoints:

### File Upload and Validation
| HTTP Method | Path | Description |
|-------------|------|-------------|
| POST | `/file-validation-api/upload` | Upload and validate a CSV file |
| GET | `/file-validation-api/healthcheck` | Health check endpoint |

### Request Parameters for Upload Endpoint
- **file**: Multipart file (CSV format required)
- **metadata**: JSON string containing file metadata with required fields:
  - `fileName`: Name of the file
  - `fromLocation`: Source location/AML body name  
  - `toLocation`: Destination location

### Request Headers
All endpoints require:
- **Authorization**: Bearer token for authentication
- **ERIC-Identity**: ERIC identity header (handled by ERIC framework)
- **ERIC-Identity-Type**: Type of ERIC identity (handled by ERIC framework)

### Response Codes
- **201 Created**: File uploaded successfully
- **400 Bad Request**: Invalid request (invalid file format, missing metadata, or validation errors)
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error


## Testing
- Unit tests: `mvn test`
- Integration tests: Run via Maven or IntelliJ
- Use Postman to test API endpoints
- Test CSV files are available in `src/test/resources/`

## Build Command
To build the repository from the pom.xml file, run:
```bash
mvn clean install
```
