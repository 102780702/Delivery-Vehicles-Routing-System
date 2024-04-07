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
                            // const jsonDataContainer = document.getElementById('jsonData');
                            // jsonDataContainer.textContent = JSON.stringify(coordinate.longitude, null, 2); debug
                            let coord = {lat: coordinate.latitude, lng: coordinate.longitude};
                            coordinateList.push(coord);
                        });
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
        var areaAData = batch['Area_A'];

        if (areaAData.length > 0) 
        {
            const coordinatesList = areaAData[0];
            coordinatesList.forEach(coordinate => {
                Area_A.push(coordinate);
            });
        }

        // const jsonDataContainer = document.getElementById('jsonData');
        // jsonDataContainer.textContent = JSON.stringify(Area_A, null, 2); debug
    }

    if (batch.hasOwnProperty('Area_B')) 
    {
        var areaAData = batch['Area_B'];

        if (areaAData.length > 0) 
        {
            const coordinatesList = areaAData[0];
            coordinatesList.forEach(coordinate => {
                Area_B.push(coordinate);
            });
        }

        // const jsonDataContainer = document.getElementById('jsonData');
        // jsonDataContainer.textContent = JSON.stringify(Area_B, null, 2); debug
    }

    // CD

    var markerLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    
    function calculateRoute(index) 
    {
        if (index >= Area_A.length - 1) 
        {
            return;
        }

        if (index >= Area_B.length - 1) 
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
            position: Area_A[index],
            map: map,
            label: markerLetters[index % markerLetters.length]
        });

        new google.maps.Marker({
            position: Area_A[index + 1],
            map: map,
            label: markerLetters[(index + 1) % markerLetters.length]
        });

        // Marker setting
        new google.maps.Marker({
            position: Area_B[index],
            map: map,
            label: markerLetters[index % markerLetters.length]
        });

        new google.maps.Marker({
            position: Area_B[index + 1],
            map: map,
            label: markerLetters[(index + 1) % markerLetters.length]
        });

        // Apply the render related stuff to map
        directionsRenderer.setMap(map);
        
        // Create a DirectionsService object.
        var directionsService = new google.maps.DirectionsService();
        
        var request = 
        {
            origin: Area_A[index],
            destination: Area_A[index + 1],
            origin: Area_B[index],
            destination: Area_B[index + 1],
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

// document.getElementById('yourButtonId').addEventListener('click', function() 
// {
//     currentIndex++;

//     if (currentIndex >= batches.length) 
//     {
//         currentIndex = 0;
//     }

//     const currentBatch = batches[currentIndex];
//     console.log('Current Batch:', currentBatch);

//     // 在這裡你可以做任何其他你想做的事情，例如更新介面或處理數據等等
// });