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
        initMap(currentIndex);
    })
    .catch(error => console.error('Error:', error));

async function initMap(batchindex) 
{
    // The location of Swinburne Sarawak
    const position = { lat: 1.532302, lng: 110.357173 };
    const positionSecondMap = { lat: 1.532302, lng: 110.357173 };

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerView } = await google.maps.importLibrary("marker");

    // Create a new div element
    const newDiv = document.createElement("div");
    const newP = document.createElement("p");

    // Set the id of the div using the counter
    newDiv.id = `map${batchindex}`;

    // Set some content for the div (optional)
    newDiv.textContent = "This is div " + batchindex;

    map = new Map(newDiv, {
        zoom: 13,
        center: position,
        mapId: `MAP_0${batchindex}`,
    });

    newDiv.style.height = "700px";
    newDiv.style.width = "100%";
    newDiv.style.backgroundColor = "#ccc";

    // Append the new div to the container div
    document.getElementById("container").appendChild(newDiv);
    document.getElementById("container").appendChild(newP);

    console.log("index: ", batchindex);
    console.log("mapdata: ", batches[batchindex]);

    var batch = batches[batchindex];
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
        const mapIndex = 0;
        function renderRouteAndMarkers(area, mapIndex, color) 
        {
            if (mapIndex >= area.length - 1) {
                return;
            }
    
            // Marker setting
            var marker = new google.maps.Marker({
                position: area[ mapIndex],
                map: map,
                label: markerLetters[mapIndex % markerLetters.length],
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
                position: area[mapIndex + 1],
                map: map,
                label: markerLetters[(mapIndex + 1) % markerLetters.length],
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
                origin: area[mapIndex],
                destination: area[mapIndex + 1],
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
            renderRouteAndMarkers(area, mapIndex + 1, color);
        }

        // Render routes and markers for each area with different colors
        renderRouteAndMarkers(Area_A, mapIndex, '#3366FF'); // Blue
        renderRouteAndMarkers(Area_B, mapIndex, '#FF3333'); // Red
        renderRouteAndMarkers(Area_C, mapIndex, '#DC6B19'); // Orange
        renderRouteAndMarkers(Area_D, mapIndex, '#D862BC'); // Purple
    
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
    

    calculateRoute(batchindex);
    if (batchindex < (batches.length - 1)){
        initMap(batchindex + 1);
    }
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