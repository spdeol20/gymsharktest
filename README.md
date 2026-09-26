# Gymshark catalogue

Android catalogue for the Gymshark mobile engineering challenge. One static CDN document, about a thousand products, shown as a shop grid and a product detail screen.

## Architecture

Single `:app` module. Presentation talks to one repository. There is no domain or use-case layer.

```
CDN JSON  ->  ProductRemoteDataSource  ->  ProductMapper  ->  Room
                                                                    |
                                              ProductListViewModel  |  Paging 3 (All products)
                                              ProductDetailViewModel |  query by id
                                              Featured shelf        |  in-stock merchandising rows
```

- The network call replaces the Room table. A failed refresh leaves the previous rows in place, and the grid shows a stale-data warning instead of going blank.
- The All products grid pages out of Room (20 per page). Pages away from the viewport are dropped. The featured carousel is a separate query, so chip filtering does not depend on which page is loaded, and the chips never filter the grid.
- Sort (catalogue order, price low to high, price high to low) is an `ORDER BY` on that paging query. The choice is kept in the list screen's saved state, so pull-to-refresh and rotation do not reset it. Equal prices keep catalogue order.
- Detail reads the product by id from Room, so the screen still works after process death without putting the product on the back stack.

Prices are minor units exactly as the payload sends them. `1000` is £10.00. The document has no currency code; the UI assumes GBP.

## Security

The client is treated as untrusted. The catalogue is public product data, not an account.

- HTTPS only. The manifest disables cleartext, the base URL is `https`, OkHttp is limited to restricted TLS, and the network security config trusts platform CAs. Release builds do not trust user-installed certificates. Debug builds do, so a proxy can be used while developing.
- The CDN certificate is not pinned. No pin set is published for that host, and a pin that outlives a certificate rotation would make the app unable to load the catalogue.
- No API keys or other secrets are compiled into the app. The catalogue host is a build config field so a staging host cannot be copied into a release build by accident.
- HTTP logging is debug-only and limited to method, URL, and status. Bodies and headers are not logged. Failures that reach the UI carry an error type, not a message or a stack trace.
- Room holds the catalogue cache only. Backup and device transfer exclude the database, shared preferences, and files. `allowBackup` is false.
- A schema change drops the table. The next refresh refills it. That is safe because the rows are not user data.
- Release builds run R8. Debug builds do not.

## Build

Open the project in Android Studio and run the `app` configuration. Unit tests are in `app/src/test` and include Robolectric coverage of the Room catalogue.

Room writes its schema to `app/schemas` on the first build that runs KSP.
