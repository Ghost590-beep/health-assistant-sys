# HealthRecordControllerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**create**](HealthRecordControllerApi.md#create) | **POST** /api/health-records |  |
| [**getByPatient**](HealthRecordControllerApi.md#getByPatient) | **GET** /api/health-records/patient/{patientId} |  |
| [**getVitals**](HealthRecordControllerApi.md#getVitals) | **GET** /api/health-records/patient/{patientId}/vitals |  |


<a id="create"></a>
# **create**
> Map&lt;String, Object&gt; create(requestBody)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HealthRecordControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    HealthRecordControllerApi apiInstance = new HealthRecordControllerApi(defaultClient);
    Map<String, Object> requestBody = null; // Map<String, Object> | 
    try {
      Map<String, Object> result = apiInstance.create(requestBody);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HealthRecordControllerApi#create");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **requestBody** | [**Map&lt;String, Object&gt;**](Object.md)|  | |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

<a id="getByPatient"></a>
# **getByPatient**
> List&lt;Map&lt;String, Object&gt;&gt; getByPatient(patientId)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HealthRecordControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    HealthRecordControllerApi apiInstance = new HealthRecordControllerApi(defaultClient);
    Integer patientId = 56; // Integer | 
    try {
      List<Map<String, Object>> result = apiInstance.getByPatient(patientId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HealthRecordControllerApi#getByPatient");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **patientId** | **Integer**|  | |

### Return type

[**List&lt;Map&lt;String, Object&gt;&gt;**](Map.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

<a id="getVitals"></a>
# **getVitals**
> List&lt;Map&lt;String, Object&gt;&gt; getVitals(patientId)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HealthRecordControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    HealthRecordControllerApi apiInstance = new HealthRecordControllerApi(defaultClient);
    Integer patientId = 56; // Integer | 
    try {
      List<Map<String, Object>> result = apiInstance.getVitals(patientId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HealthRecordControllerApi#getVitals");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **patientId** | **Integer**|  | |

### Return type

[**List&lt;Map&lt;String, Object&gt;&gt;**](Map.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

