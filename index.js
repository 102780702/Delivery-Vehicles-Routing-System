// Initialize and add the map
let map;

async function initMap() 
{
  // The location of Swinburne Sarawak
  const position = { lat: 1.532302, lng: 110.357173 };

  const { Map } = await google.maps.importLibrary("maps");
  const { AdvancedMarkerView } = await google.maps.importLibrary("marker");

  // The map, centered at Swinburne Sarawak
  map = new Map(document.getElementById("map"), {
    zoom: 13,
    center: position,
    mapId: "MAP_01",
  });

  // The marker, positioned at Swinburne Sarawak
  const marker = new AdvancedMarkerView({
    map: map,
    position: position,
    title: "Swinburne University of Technology Sarawak Campus",
  });
}

initMap();

// Create a DirectionsService object.
var directionsService = new google.maps.DirectionsService();

// Create a DirectionsRenderer object.
var directionsRenderer = new google.maps.DirectionsRenderer({
  map: map
});

// Calculate the route between each point in the list.
var points = [
  {lat: 1.532302, lng: 110.357173},
  {lat: 1.535546, lng: 110.358202},
  {lat: 1.46668, lng: 110.425148},
];

for (var i = 0; i < points.length - 1; i++) {
  directionsService.route({
    origin: points[i],
    destination: points[i + 1],
    travelMode: google.maps.TravelMode.DRIVING
  }, function(response, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      // Render the route on the map.
      directionsRenderer.setDirections(response);
    }
  });
}

