package com.example.uzradyab.core.debug

import com.example.uzradyab.core.network.MockConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockTraccarInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!MockConfig.USE_MOCK_DATA) {
            return chain.proceed(chain.request())
        }

        val uri = chain.request().url.toUri().toString()

        val responseString = when {
            uri.contains("api/session") -> {
                if (chain.request().method == "DELETE") "" else SESSION_JSON
            }
            uri.contains("api/devices") -> DEVICES_JSON
            uri.contains("api/positions") -> {
                if (uri.contains("from=") || uri.contains("to=")) {
                    ROUTE_POSITIONS_JSON
                } else {
                    POSITIONS_JSON
                }
            }
            uri.contains("api/reports/summary") -> SUMMARY_REPORT_JSON
            else -> return chain.proceed(chain.request())
        }

        // Add a slight delay to simulate network
        // Thread.sleep(500)

        return Response.Builder()
            .code(200)
            .message(responseString)
            .request(chain.request())
            .protocol(Protocol.HTTP_1_0)
            .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("content-type", "application/json")
            .build()
    }

    companion object {
        private const val SESSION_JSON = """
            {
              "id": 1,
              "name": "کاربر تست",
              "email": "09123456789",
              "phone": "09123456789",
              "readonly": false,
              "administrator": true,
              "map": "",
              "latitude": 35.6892,
              "longitude": 51.3890,
              "zoom": 12,
              "attributes": {}
            }
        """

        private const val DEVICES_JSON = """
            [
              {
                "id": 1,
                "name": "دستگاه تستی ۱",
                "uniqueId": "123456789012345",
                "status": "online",
                "lastUpdate": "2026-06-11T12:00:00Z",
                "positionId": 1,
                "groupId": 0,
                "phone": "",
                "model": "TestModel",
                "contact": "",
                "category": "car",
                "disabled": false,
                "attributes": {}
              }
            ]
        """

        private const val POSITIONS_JSON = """
            [
              {
                "id": 1,
                "deviceId": 1,
                "protocol": "osmand",
                "serverTime": "2026-06-11T12:00:00Z",
                "deviceTime": "2026-06-11T12:00:00Z",
                "fixTime": "2026-06-11T12:00:00Z",
                "valid": true,
                "latitude": 35.7000,
                "longitude": 51.4000,
                "altitude": 1200.0,
                "speed": 60.5,
                "course": 90.0,
                "address": "تهران، میدان ولیعصر",
                "accuracy": 0.0,
                "network": null,
                "attributes": {
                  "ignition": true,
                  "batteryLevel": 85,
                  "distance": 15.2,
                  "totalDistance": 15000.5,
                  "motion": true
                }
              }
            ]
        """
        
        private val ROUTE_POSITIONS_JSON = buildString {
            append("[\n")
            var lat = 35.6892
            var lon = 51.3890
            val count = 120
            for (i in 0 until count) {
                // A simple curvy path
                lat += 0.0005 * kotlin.math.cos(i / 10.0)
                lon += 0.0005 * kotlin.math.sin(i / 10.0)
                
                // Construct a mock timestamp
                val hour = 10 + (i / 60)
                val min = (22 + i) % 60
                val time = "2026-06-11T${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}:00Z"
                
                append("""
                  {
                    "id": ${i + 1},
                    "deviceId": 1,
                    "protocol": "osmand",
                    "serverTime": "$time",
                    "deviceTime": "$time",
                    "fixTime": "$time",
                    "valid": true,
                    "latitude": $lat,
                    "longitude": $lon,
                    "altitude": 1200.0,
                    "speed": ${40 + (i % 20)},
                    "course": ${(i * 5) % 360},
                    "address": "مسیر تستی",
                    "accuracy": 0.0,
                    "network": null,
                    "attributes": {
                      "ignition": true,
                      "distance": 15.2,
                      "motion": true
                    }
                  }${if (i < count - 1) "," else ""}
                """)
            }
            append("\n]")
        }

        private const val SUMMARY_REPORT_JSON = """
            [
                {
                    "deviceId": 1,
                    "deviceName": "دستگاه تستی ۱",
                    "maxSpeed": 80.0,
                    "averageSpeed": 40.0,
                    "distance": 25.0,
                    "spentFuel": 2.5,
                    "engineHours": 7200000
                }
            ]
        """
    }
}
