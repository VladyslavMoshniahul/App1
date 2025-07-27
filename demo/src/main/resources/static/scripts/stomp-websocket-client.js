const WEBSOCKET_ENDPOINT = '/ws'; 

let stompClient = null;

function connectStompWebSocket() {
    const socket = new SockJS(WEBSOCKET_ENDPOINT);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/schools/new', (message) => {
            console.log('Received new school:', JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/users/new', (message) => {
            console.log('Received new user:', JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/users/updated', (message) => {
            console.log('User updated:', JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/class/{classId}/tasks/new', (message) => {
            console.log('New task in class:', JSON.parse(message.body));
        });

        stompClient.subscribe('/user/queue/profileUpdate', (message) => {
            console.log('Your profile was updated:', JSON.parse(message.body));
        });

        stompClient.subscribe('/user/queue/taskUpdates', (message) => {
            console.log('Your task status updated:', JSON.parse(message.body));
        });

        stompClient.subscribe('/user/queue/errors', (message) => {
            console.error('Personal error message:', message.body);
        });

    }, (error) => {
        console.error('STOMP connection error:', error);
        setTimeout(connectStompWebSocket, 5000);
    });
}

function disconnectStompWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

document.addEventListener('DOMContentLoaded', connectStompWebSocket);