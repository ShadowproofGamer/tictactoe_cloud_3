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
const loginButton = document.getElementById('loginButton');
const usernamePage = document.getElementById('usernamePage')
let currentPlayer = X_CLASS;
let gameActive = false;
let username = null;
let userGUID = null;
let stompClient = null;
let personalConnection = null;
let roomConnection = null;
let roomNumber = null;
let startingPlayer = null;


function connect(event) {
    username = document.querySelector('#nickname').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        gamePage.classList.remove('hidden');

        const socket = new SockJS('http://localhost:8080/ws');
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
    personalConnection = stompClient.subscribe('/topic/' + username, onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/topic/login",
        {},
        JSON.stringify({type: 'LOGIN', username: username, content: "Want to join"})
    )
    cells.forEach(cell => {
        cell.innerHTML = ""
        cell.classList.remove(X_CLASS);
        cell.classList.remove(O_CLASS);
        cell.removeEventListener('click', handleClick);
    });
    statusDisplay.innerText = `Waiting for game...`;
}

function onLeave() {
    if (roomNumber != null) {
        roomConnection.unsubscribe();
        stompClient.send("/app/room/" + roomNumber,
            {},
            JSON.stringify({type: 'LEAVE', username: userGUID, content: "Want to leave"})
        );
        roomNumber = null;
        startingPlayer = null;
    }
    cells.forEach(cell => {
        cell.innerHTML = ""
        cell.classList.remove(X_CLASS);
        cell.classList.remove(O_CLASS);
        cell.removeEventListener('click', handleClick);
    });
    onRestart()
}

function onRestart() {


    //Tell your username to the server
    stompClient.send("/app/topic/lobby",
        {},
        JSON.stringify({type: 'START', username: userGUID, content: "Want to join"})
    )
    statusDisplay.innerText = `Waiting for game...`;
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log(message);


    if (message.type === 'LOGIN') {
        userGUID = message.username;
        personalConnection.unsubscribe()
        personalConnection = stompClient.subscribe('/topic/' + userGUID, onMessageReceived)
        console.log(message.username + ' joined!');


        // Tell the server, that you want to start a game
        stompClient.send("/app/topic/lobby",
            {},
            JSON.stringify({type: 'START', username: userGUID, content: "Want to start the game"})
        )
    } else if (message.type === 'LEAVE') {
        gameActive = false;
        cells.forEach(cell => {
            cell.removeEventListener('click', handleClick);
        });
        statusDisplay.innerText = 'Rival left!\nRestart to play again!';
        console.log(message.username + ' left!\nRestart if you want to play again!');
        roomConnection.unsubscribe();
        roomNumber = null;
        startingPlayer = null;
    } else if (message.type === 'ROOM') {
        // console.log(message.roomNumber);
        if (startingPlayer == null && message.playerStarting != null) {
            startingPlayer = message.playerStarting;
            if (startingPlayer === userGUID) startGame();
            else statusDisplay.innerText = `Rival's turn`;
        }
        if (roomNumber == null) {
            roomNumber = message.roomNumber;
            roomConnection = stompClient.subscribe("/topic/room/" + roomNumber, onMessageReceived);
        }

    } else if (message.type === 'START') {
        console.log(startingPlayer)


    } else if (message.type === 'MOVE' && message.username !== userGUID) {
        console.log('Received opponent move from server:', message.content);
        statusDisplay.innerText = `Your turn`;
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
    // placeMark(cell, X_CLASS);


    // Disable further clicks until opponent's move is received
    cells.forEach(cell => {
        cell.removeEventListener('click', handleClick);
    });

    // Send move data to server
    const cellIndex = parseInt(Array.from(cells).indexOf(cell));

    updateBoard(cellIndex, X_CLASS);
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
        statusDisplay.innerText = currentClass === X_CLASS ? "You win!" : "Rival wins!";
    }
}

function sendMoveToServer(cellIndex) {
    console.log('Sending move data to server:', cellIndex);
    stompClient.send("/app/room/" + roomNumber,
        {},
        JSON.stringify({type: "MOVE", username: userGUID, content: cellIndex.toString()})
    );
    if (gameActive) statusDisplay.innerText = `Rival's turn`;
}

function updateBoard(cellIndex, currentPlayer) {
    const cell = cells[cellIndex];
    const currentClass = currentPlayer === X_CLASS ? X_CLASS : O_CLASS;
    placeMark(cell, currentClass);
    if (!checkWin(currentClass) && !isDraw() && currentClass === O_CLASS) {
        // If the game is not over, enable player to make another move
        cells.forEach(cell => {
            cell.addEventListener('click', handleClick, {once: true});
        });
    }
}

// leaveButton.addEventlistener('click', onLeave)
restartButton.addEventListener('click', onLeave);
loginButton.addEventListener('click', connect);