<b>Bc Booze Finder</b> is an Android app that plots liquor stores in British Columbia nearest to a point of interest. A point of interest can either be a user chosen point or the user's current location.

The data for the liquor stores was initially obtained from http://www.data.gov.bc.ca/. However, because the data only contained an address, and not the latitude and longtitude, the data first had to be parsed and translatd to a useable format which contained the latitudes and longitudes. This was done 'offline' in a separate Java project using Google's Geocoding API. Details for this parsing step can be found at https://github.com/ben20/bcliquor-parser.







