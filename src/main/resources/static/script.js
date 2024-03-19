// const socket = new SockJS("ws://localhost:8080/ws");
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
let gameActive = true;
let nickname = null;
var stompClient = null;
var username = "Shadow";
var roomNumber = null;


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
    stompClient.send("/topic/lobby",
        {},
        JSON.stringify({type: 'JOIN', username: username, content: "Want to join"})
    )

}


// function sendMessage(event) {
//     var messageContent = messageInput.value.trim();
//     if(messageContent && stompClient) {
//         var chatMessage = {
//             sender: username,
//             content: messageInput.value,
//             type: 'CHAT'
//         };
//         stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
//         messageInput.value = '';
//     }
//     event.preventDefault();
// }


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');
    console.log(message);
    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        console.log(message.username + ' joined!');
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        console.log(message.username + ' left!');
    } else if (message.type === 'ROOM') {
        console.log(message.roomNumber);
        roomNumber = message.roomNumber;
    }
}


// let stompClient = null;
// let selectedUserId = null;
//
// function connect(event) {
//     nickname = document.querySelector('#nickname').value.trim();
//
//     if (nickname) {
//         usernamePage.classList.add('hidden');
//         gamePage.classList.remove('hidden');
//
//         const socket = new SockJS('/ws');
//         stompClient = Stomp.over(socket);
//
//         stompClient.connect({}, onConnected, onError);
//     }
//     event.preventDefault();
// }
// function onConnected() {
//     stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived);
//     stompClient.subscribe(`/user/public`, onMessageReceived);
//
//     // register the connected user
//     stompClient.send("/app/user.addUser",
//         {},
//         JSON.stringify({nickName: nickname, status: 'WAITING'})
//     );
//     findAndDisplayConnectedUsers().then();
// }
//
// socket.onopen = function(event) {
//     console.log("WebSocket opened");
//     socket.send("Hello, Server!");
// };
//
// socket.onmessage = function(event) {
//     console.log("Received message: " + event.data);
// };
//
// socket.onclose = function(event) {
//     console.log("WebSocket closed");
// };
//
// socket.onerror = function(event) {
//     console.error("WebSocket error: " + event);
// };
//


function mockLogin() {
    nickname = document.querySelector('#nickname').value.trim();

    if (nickname) {
        usernamePage.classList.add('hidden');
        gamePage.classList.remove('hidden');

    }
}

startGame();

restartButton.addEventListener('click', startGame);

function startGame() {
    gameActive = true;
    currentPlayer = X_CLASS;
    statusDisplay.innerText = `${currentPlayer}'s turn`;
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
    const currentClass = currentPlayer === X_CLASS ? X_CLASS : O_CLASS;
    placeMark(cell, currentClass);

    // Disable further clicks until opponent's move is received
    cells.forEach(cell => {
        cell.removeEventListener('click', handleClick);
    });

    // Send move data to server
    const cellIndex = Array.from(cells).indexOf(cell);
    sendMoveToServer(cellIndex);

    if (checkWin(currentClass)) {
        endGame(false);
    } else if (isDraw()) {
        endGame(true);
    }

}

function placeMark(cell, currentClass) {
    cell.classList.add(currentClass);
    cell.innerText = currentClass
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

function endGame(draw) {
    gameActive = false;
    if (draw) {
        statusDisplay.innerText = `It's a Draw!`;
    } else {
        statusDisplay.innerText = `${currentPlayer} Wins!`;
    }
    cells.forEach(cell => {
        cell.removeEventListener('click', handleClick);
    });
}

function sendMoveToServer(cellIndex) {
    // Example code to send move data to server
    // Replace this with your actual implementation
    const moveData = {cellIndex, currentPlayer};
    console.log('Sending move data to server:', moveData);
    //socket.send(JSON.stringify(moveData))

    const opponentMove = getOpponentMoveFromServer();
    console.log('Received opponent move from server:', opponentMove);
    updateBoard(opponentMove.cellIndex, opponentMove.currentPlayer);

}

function getOpponentMoveFromServer() {
    // This function should return an object with 'cellIndex' and 'currentPlayer'
    // representing the opponent's move
    return {cellIndex: getRandomEmptyCellIndex(), currentPlayer: O_CLASS}; // Example: Opponent's move is randomly chosen
}

function getRandomEmptyCellIndex() {
    // Example function to get a random empty cell index
    // Replace this with your actual implementation
    // This function should return the index of an empty cell on the board
    const emptyCells = [...cells].filter(cell => !cell.classList.contains(X_CLASS) && !cell.classList.contains(O_CLASS));
    const randomIndex = Math.floor(Math.random() * emptyCells.length);
    return Array.from(cells).indexOf(emptyCells[randomIndex]);
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

