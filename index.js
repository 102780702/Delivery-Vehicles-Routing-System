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

    var points = 
    [
        {lat: 1.532302, lng: 110.357173},
        {lat: 1.507103, lng: 110.360874},
        {lat: 1.535546, lng: 110.358202},
        {lat: 1.46668, lng: 110.425148},
    ];

    var markerLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    
    function calculateRoute(index) 
    {
        if (index >= points.length - 1) 
        {
            return;
        }
        
        // Create a DirectionsRenderer object and setting
        var directionsRenderer = new google.maps.DirectionsRenderer
        ({
            suppressMarkers: true,
            polylineOptions: {
                strokeColor: '#D862BC'
            }
        });

        // Marker setting
        new google.maps.Marker({
            position: points[index],
            map: map,
            label: markerLetters[index % markerLetters.length]
        });

        new google.maps.Marker({
            position: points[index + 1],
            map: map,
            label: markerLetters[(index + 1) % markerLetters.length]
        });

        // Apply the render related stuff to map
        directionsRenderer.setMap(map);
        
        // Create a DirectionsService object.
        var directionsService = new google.maps.DirectionsService();

        var request = 
        {
            origin: points[index],
            destination: points[index + 1],
            travelMode: google.maps.TravelMode.DRIVING
        };
    
        directionsService.route(request, function(response, status) 
        {
            if (status == google.maps.DirectionsStatus.OK) 
            {
                directionsRenderer.setDirections(response);
            }
            calculateRoute(index + 1);
        });
    }
    
    calculateRoute(0);
}

initMap();