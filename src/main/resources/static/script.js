const X_CLASS = 'x';
const O_CLASS = 'o';
const WINNING_COMBINATIONS = [
    [0, 1, 2],
    [3, 4, 5],
    [6, 7, 8],
    [0, 3, 6],
    [1, 4, 7],
    [2, 5, 8],
    [0, 4, 8],
    [2, 4, 6]
];
const gamePage = document.getElementById('gamePage')
const board = document.getElementById('board');
const cells = document.querySelectorAll('.cell');
const statusDisplay = document.getElementById('status');
const restartButton = document.getElementById('restartButton');
const usernamePage = document.getElementById('usernamePage')
let currentPlayer = X_CLASS;
let gameActive = false;
let username = null;
var stompClient = null;
var roomNumber = null;
var startingPlayer = null;


function connect(event) {
    username = document.querySelector('#nickname').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        gamePage.classList.remove('hidden');

        var socket = new SockJS('http://localhost:8080/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onError() {
    console.log("Stomp error")
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/' + username, onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/topic/lobby",
        {},
        JSON.stringify({type: 'START', username: username, content: "Want to join"})
    )
    cells.forEach(cell => {
        cell.innerHTML = ""
        cell.classList.remove(X_CLASS);
        cell.classList.remove(O_CLASS);
        cell.removeEventListener('click', handleClick);
    });
    statusDisplay.innerText = `Waiting for game...`;
}

function onRestart(){
    stompClient.send("/app/room/"+roomNumber,
    {},
    JSON.stringify({type:'LEAVE', username: username, content: "Want to leave"})
    );
    stompClient.unsubscribe("/room/"+roomNumber);
    roomNumber = null;
    startingPlayer = null;
    cells.forEach(cell => {
        cell.innerHTML = ""
        cell.classList.remove(X_CLASS);
        cell.classList.remove(O_CLASS);
        cell.removeEventListener('click', handleClick);
    });

    // Tell your username to the server
    stompClient.send("/app/topic/lobby",
        {},
        JSON.stringify({type: 'START', username: username, content: "Want to join"})
    )
    statusDisplay.innerText = `Waiting for game...`;
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');
    console.log(message);
    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        console.log(message.username + ' joined!');
    } else if (message.type === 'LEAVE') {
        gameActive = false;
        cells.forEach(cell => {
            cell.removeEventListener('click', handleClick);
        });
        statusDisplay.innerText = message.username + ' left!\nRestart if you want to play again!';
        console.log(message.username + ' left!\nRestart if you want to play again!');
        stompClient.send("/app/room/"+roomNumber,
            {},
            JSON.stringify({type:'LEAVE', username: username, content: "Want to leave"})
        );
        stompClient.unsubscribe("/room/"+roomNumber);
        roomNumber = null;
        startingPlayer = null;
    } else if (message.type === 'ROOM') {
        console.log(message.roomNumber);
        if (startingPlayer==null){
            startingPlayer = message.playerStarting;
        }
        if (roomNumber==null){
            roomNumber = message.roomNumber;
            stompClient.subscribe("/topic/room/" + roomNumber, onMessageReceived);
        }
    } else if (message.type === 'START') {
        console.log(startingPlayer)
        if (startingPlayer===username) startGame();
        else statusDisplay.innerText = `Rival's turn`;
    } else if (message.type === 'MOVE' && message.username !== username) {
        console.log('Received opponent move from server:', message.content);
        if (!gameActive) startGame();
        updateBoard(message.content, O_CLASS);
    }
}



function startGame() {
    gameActive = true;
    statusDisplay.innerText = `Your turn`;
    cells.forEach(cell => {
        cell.innerHTML = ""
        cell.classList.remove(X_CLASS);
        cell.classList.remove(O_CLASS);
        cell.removeEventListener('click', handleClick);
        cell.addEventListener('click', handleClick, {once: true});
    });
}



function handleClick(e) {
    const cell = e.target;
    placeMark(cell, X_CLASS);

    // Disable further clicks until opponent's move is received
    cells.forEach(cell => {
        cell.removeEventListener('click', handleClick);
    });

    // Send move data to server
    const cellIndex = parseInt(Array.from(cells).indexOf(cell));
    sendMoveToServer(cellIndex);

}

function placeMark(cell, currentClass) {
    cell.classList.add(currentClass);
    cell.innerText = currentClass
    if (checkWin(currentClass)) {
        endGame(false, currentClass);
    } else if (isDraw()) {
        endGame(true, currentClass);
    }
}

function checkWin(currentClass) {
    return WINNING_COMBINATIONS.some(combination => {
        return combination.every(index => {
            return cells[index].classList.contains(currentClass);
        });
    });
}

function isDraw() {
    return [...cells].every(cell => {
        return cell.classList.contains(X_CLASS) || cell.classList.contains(O_CLASS);
    });
}

function endGame(draw, currentClass) {
    gameActive = false;
    cells.forEach(cell => {
        cell.removeEventListener('click', handleClick);
    });
    if (draw) {
        statusDisplay.innerText = `It's a Draw!`;
    } else {
        statusDisplay.innerText = currentClass===X_CLASS?"You win!":"Rival wins!";
    }
}

function sendMoveToServer(cellIndex) {
    console.log('Sending move data to server:', cellIndex);
    stompClient.send("/app/room/" + roomNumber,
        {},
        JSON.stringify({type: "MOVE", username: username, content: cellIndex.toString()})
    );

}

function updateBoard(cellIndex, currentPlayer) {
    const cell = cells[cellIndex];
    const currentClass = currentPlayer === X_CLASS ? X_CLASS : O_CLASS;
    placeMark(cell, currentClass);
    if (!checkWin(currentClass) && !isDraw()) {
        // If the game is not over, enable player to make another move
        cells.forEach(cell => {
            cell.addEventListener('click', handleClick, {once: true});
        });
    }
}

restartButton.addEventListener('click', onRestart);