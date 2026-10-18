# DriveFlow Reader Config JSON Schema

## Overview
DriveFlow uses external JSON configuration files to resolve Android Accessibility view resource IDs and parsing fallbacks dynamically. This allows updating or overriding target view IDs after ride-app releases (e.g. via the Settings/Diagnostics import feature) without recompiling or shipping a new APK.

## Schema Fields
| Field | Type | Description |
|---|---|---|
| `minRevision` | Integer | Minimum schema format version required by the app. |
| `appCode` | String | Internal identifier (`indrive`, `uber`, `didi`). |
| `packageName` | String | Target Android application package name. |
| `appVersion` | String | Target tested version of the ride app. |
| `idPrefix` | String | View ID namespace prefix (e.g. `sinet.startup.inDriver:id/`). |
| `ids` | Object | Map of semantic field names to specific view resource IDs. |
| `fallbacks` | Object | Map of field names to fallback strategies (alternative IDs and text patterns). |

## ID Map Semantics (`ids`)
### LIST Mode
- `list_recycler`: RecyclerView holding offer cards (`orders_main_recyclerview_orders`).
- `list_card`: Root container for each offer card (`item_order_container`).
- `list_fare`: Fare amount TextView (`info_textview_stage_price_view`).
- `list_pickup_distance`: Pickup distance away from driver (`order_info_stage_textview_distance`).
- `list_rider_trips`: Rider completed rides count (`driver_common_textview_rating_rides_done`).
- `list_rider_rating`: Rider rating badge (`driver_common_textview_rating`).
- `list_request_age`: Elapsed time since offer was posted (`item_order_textview_posted_time_ago`).
- `list_from_address`: Pickup address TextView (`order_info_textview_from_address`).
- `list_to_address`: Destination address TextView (`order_info_textview_to_addresses`).

### DETAIL Mode
- `detail_bottom_sheet`: Bottom sheet container node (`design_bottom_sheet`).
- `detail_container`: Order details container (`order_info_container`).
- `detail_fare`: Detailed fare amount TextView.
- `detail_pickup_distance`: Pickup distance TextView.
- `detail_from_address`: Detailed pickup location (`info_textview_pickup`).
- `detail_to_address`: Detailed destination location (`info_textview_destination`).
- `detail_extra_stops`: RecyclerView containing intermediate stops (`info_recyclerview_extra_stops`).
- `detail_rider_note`: Rider special request note (`order_info_textview_description`).
- `detail_offer_price_button`: Price highlight action button (`bid_btn_price_highlighted`). Text read only, never clicked.
- `detail_map`: Main ride route map container (`driver_order_container_map`).
- `detail_map_interceptor`: Map touch interceptor node (`driver_order_container_map_interceptor`).

## Fallback Mechanism
If a view ID fails to resolve on screen:
1. The reader queries `fallbacks[fieldName].alternativeIds`.
2. If unresolved, it scans node texts in the card hierarchy using `fallbacks[fieldName].textPattern`.
3. If still unresolved, it increments the local diagnostic counter `capture_fail:<fieldName>`.
4. The reader continues parsing remaining fields without crashing. No rider data is logged or stored.
