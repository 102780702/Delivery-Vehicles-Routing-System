let map;
let batches = [];
let currentIndex = 0;

fetch('http://localhost:8080')
    .then(response => response.json())
    .then(data => {
        data.forEach(map => {
            let mapData = {};
            for (const areaKey in map) 
            {
                if (Object.hasOwnProperty.call(map, areaKey)) 
                {
                    const areaData = map[areaKey];
                    let area = [];
                    areaData.forEach(coordinates => {
                        let coordinateList = [];
                        coordinates.forEach(coordinate => {
                            let coord = {lat: coordinate.latitude, lng: coordinate.longitude};
                            coordinateList.push(coord);
                        })
                        area.push(coordinateList);
                    });
                        
                    mapData[areaKey] = area;
                }
            }
            batches.push(mapData);
        });
        initMap();
    })
    .catch(error => console.error('Error:', error));

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

    var batch = batches[currentIndex];
    let Area_A = []
    let Area_B = []
    let Area_C = []
    let Area_D = []

    if (batch.hasOwnProperty('Area_A')) 
    {
        var areaData = batch['Area_A'];

        if (areaData.length > 0) 
        {
            const coordinatesList = areaData[0];
            coordinatesList.forEach(coordinate => {
                Area_A.push(coordinate);
            });
        }
    }

    if (batch.hasOwnProperty('Area_B')) 
    {
        var areaData = batch['Area_B'];

        if (areaData.length > 0) 
        {
            const coordinatesList = areaData[0];
            coordinatesList.forEach(coordinate => {
                Area_B.push(coordinate);
            });
        }
    }

    if (batch.hasOwnProperty('Area_C')) 
    {
        var areaData = batch['Area_C'];

        if (areaData.length > 0) 
        {
            const coordinatesList = areaData[0];
            coordinatesList.forEach(coordinate => {
                Area_C.push(coordinate);
            });
        }
        // const jsonDataContainer = document.getElementById('jsonData');
        // jsonDataContainer.textContent = JSON.stringify(Area_C, null, 2);
    }
    
    if (batch.hasOwnProperty('Area_D')) 
    {
        var areaData = batch['Area_D'];

        if (areaData.length > 0) 
        {
            const coordinatesList = areaData[0];
            coordinatesList.forEach(coordinate => {
                Area_D.push(coordinate);
            });
        }
    }
    
    function calculateRoute(index) 
    {
        // Array to hold all DirectionsRenderer objects
        var directionsRenderers = [];
    
        // Create a DirectionsService object.
        var directionsService = new google.maps.DirectionsService();
    
        // Define marker letters
        var markerLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    
        // Function to render route and markers for an area
        function renderRouteAndMarkers(area, index, color) 
        {
            if (index >= area.length - 1) {
                return;
            }
    
            // Marker setting
            var marker = new google.maps.Marker({
                position: area[index],
                map: map,
                label: markerLetters[index % markerLetters.length],
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    fillColor: color,
                    fillOpacity: 1,
                    strokeColor: '#000',
                    strokeWeight: 1,
                    scale: 10
                }
            });
    
            var nextMarker = new google.maps.Marker({
                position: area[index + 1],
                map: map,
                label: markerLetters[(index + 1) % markerLetters.length],
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    fillColor: color,
                    fillOpacity: 1,
                    strokeColor: '#000',
                    strokeWeight: 1,
                    scale: 10
                }
            });
    
            var directionsRenderer = new google.maps.DirectionsRenderer({
                suppressMarkers: true,
                polylineOptions: {
                    strokeColor: color
                }
            });
    
            directionsRenderer.setMap(map);
    
            var request = 
            {
                origin: area[index],
                destination: area[index + 1],
                travelMode: google.maps.TravelMode.DRIVING
            };
    
            directionsService.route(request, function(response, status) {
                if (status == google.maps.DirectionsStatus.OK) 
                {
                    directionsRenderer.setDirections(response);
                }
            });
    
            // Add the DirectionsRenderer object to the array
            directionsRenderers.push(directionsRenderer);
    
            // Render next route and markers
            renderRouteAndMarkers(area, index + 1, color);
        }

        // Render routes and markers for each area with different colors
        renderRouteAndMarkers(Area_A, index, '#3366FF'); // Blue
        renderRouteAndMarkers(Area_B, index, '#FF3333'); // Red
        renderRouteAndMarkers(Area_C, index, '#DC6B19'); // Orange
        renderRouteAndMarkers(Area_D, index, '#D862BC'); // Purple
    
        // Function to check if all routes have been rendered
        function checkAllRoutesRendered() 
        {
            for (var i = 0; i < directionsRenderers.length; i++) 
            {
                if (!directionsRenderers[i].getDirections()) 
                {
                    return false;
                }
            }
            return true;
        }
    
        // Check if all routes have been rendered
        var checkInterval = setInterval(function() 
        {
            if (checkAllRoutesRendered()) 
            {
                clearInterval(checkInterval);
                // All routes have been rendered, do something if needed
            }
        }, 100);
    }
    
    calculateRoute(0);
}

// document.getElementById('yourButtonId').addEventListener('click', function() 
// {
//     currentIndex++;

//     if (currentIndex >= batches.length) 
//     {
//         currentIndex = 0;
//     }

//     const currentBatch = batches[currentIndex];
//     console.log('Current Batch:', currentBatch);

//     
// });