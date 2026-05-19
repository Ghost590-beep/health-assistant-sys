# AppointmentControllerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**book**](AppointmentControllerApi.md#book) | **POST** /api/appointments |  |
| [**cancel**](AppointmentControllerApi.md#cancel) | **PATCH** /api/appointments/{id}/cancel |  |
| [**confirm**](AppointmentControllerApi.md#confirm) | **PATCH** /api/appointments/{id}/confirm |  |
| [**getAll2**](AppointmentControllerApi.md#getAll2) | **GET** /api/appointments |  |
| [**getByPatient1**](AppointmentControllerApi.md#getByPatient1) | **GET** /api/appointments/patient/{patientId} |  |
| [**getUpcoming**](AppointmentControllerApi.md#getUpcoming) | **GET** /api/appointments/upcoming |  |


<a id="book"></a>
# **book**
> Map&lt;String, Object&gt; book(requestBody)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    Map<String, Object> requestBody = null; // Map<String, Object> | 
    try {
      Map<String, Object> result = apiInstance.book(requestBody);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#book");
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

<a id="cancel"></a>
# **cancel**
> Map&lt;String, Object&gt; cancel(id)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    Integer id = 56; // Integer | 
    try {
      Map<String, Object> result = apiInstance.cancel(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#cancel");
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
| **id** | **Integer**|  | |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

<a id="confirm"></a>
# **confirm**
> Map&lt;String, Object&gt; confirm(id)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    Integer id = 56; // Integer | 
    try {
      Map<String, Object> result = apiInstance.confirm(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#confirm");
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
| **id** | **Integer**|  | |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

<a id="getAll2"></a>
# **getAll2**
> List&lt;Map&lt;String, Object&gt;&gt; getAll2()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    try {
      List<Map<String, Object>> result = apiInstance.getAll2();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#getAll2");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

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

<a id="getByPatient1"></a>
# **getByPatient1**
> List&lt;Map&lt;String, Object&gt;&gt; getByPatient1(patientId)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    Integer patientId = 56; // Integer | 
    try {
      List<Map<String, Object>> result = apiInstance.getByPatient1(patientId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#getByPatient1");
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

<a id="getUpcoming"></a>
# **getUpcoming**
> List&lt;Map&lt;String, Object&gt;&gt; getUpcoming()



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AppointmentControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    AppointmentControllerApi apiInstance = new AppointmentControllerApi(defaultClient);
    try {
      List<Map<String, Object>> result = apiInstance.getUpcoming();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AppointmentControllerApi#getUpcoming");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

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

