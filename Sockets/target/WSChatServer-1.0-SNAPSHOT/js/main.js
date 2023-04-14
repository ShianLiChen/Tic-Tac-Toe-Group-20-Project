let ws;
function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let code = document.getElementById("room-code").value;
    if(code.length == 0)
    {
        let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
        fetch(callURL, {
            method: 'GET',
            headers: {
                'Accept': 'text/plain',
            },
        })
            .then(response => response.text())
            .then(response => enterRoom(response)); // enter the room with the code
    }
    else
    {
        enterRoom(code);
    }

}
function enterRoom(code){
    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/"+code);

    document.getElementById("room").innerHTML = "<h3>You are chatting in the room "+code+" </h3>";
    document.getElementById("room").innerHTML += "<h3><button id=\"refresh\" onclick=\"showRooms()\">Refresh</button></h3>";

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
        }
    setTimeout(function() {
        showRooms();
    }, 100);
}
function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}

document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        let request = {"type":"chat", "msg":event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});

function showRooms()
{
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/testing";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(response => displayRooms(response));
}

function displayRooms(data)
{
    let listOfWords = data.split(",");
    listOfWords = listOfWords.filter(function(e){ return e.replace(/(\r\n|\n|\r)/gm,"")});
    document.getElementById("roomList").innerHTML = "";
    listOfWords.forEach(element => addBox(element));
}

function addBox(element)
{
    let box = document.getElementById("roomList");
    box.innerHTML += `<button onclick=switchRoom(\"${element}\")>${element}</button> <br> <br>`;
}

function switchRoom(code)
{
    ws.close();
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/"+code);

    let logURL = "http://localhost:8080/ChatResourceAPI-1.0-SNAPSHOT/api/history/"+code;
    fetch(logURL, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
        },
    })
        .then(response => response.json())
        .then(response => recoverChat(response.log));

    document.getElementById("room").innerHTML = "<h3>You are chatting in the room "+code+" </h3>";
    document.getElementById("room").innerHTML += "<h3><button id=\"refresh\" onclick=\"showRooms()\">Refresh</button></h3>";
    ws.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
    }
}

function recoverChat(data)
{
    document.getElementById("log").value += data;
}


window.onload = showRooms();