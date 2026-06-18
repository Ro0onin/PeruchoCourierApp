<?php

header("Content-Type: application/json");
include "db.php";

if (!isset($_GET['driver_email'])) {
    echo json_encode([
        "success" => false,
        "message" => "Falta el correo del repartidor"
    ]);
    exit();
}

$driver_email = mysqli_real_escape_string($conn, $_GET['driver_email']);

$sql = "SELECT id, user_email, origen, destino, descripcion, peso, metodo_pago, total, estado, created_at, driver_email
        FROM orders
        WHERE driver_email = '$driver_email'
        AND estado IN ('asignado', 'recogiendo', 'en_camino')
        ORDER BY id DESC
        LIMIT 1";

$result = mysqli_query($conn, $sql);

if (!$result) {
    echo json_encode([
        "success" => false,
        "message" => "Error al consultar pedido activo"
    ]);
    exit();
}

if (mysqli_num_rows($result) === 0) {
    echo json_encode([
        "success" => false,
        "message" => "No hay pedido activo"
    ]);
    exit();
}

$order = mysqli_fetch_assoc($result);

echo json_encode([
    "success" => true,
    "order" => $order
]);

?>