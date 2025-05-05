import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "bootstrap/dist/css/bootstrap.min.css";
import { Container, Row, Col, Navbar } from "react-bootstrap";

function App() {
  return (
    <>
      <Navbar bg="dark" variant="dark">
        <Container fluid>
          <Navbar.Brand className="ms-3">RDB - WeatherApp</Navbar.Brand>
        </Container>
      </Navbar>

      <Container fluid className="vh-100">
        <Row className="h-100">
          <Col md={3} className="bg-light p-3 border-end"></Col>
          <Col md={9} className="p-0">
            <MapContainer
              center={[51.505, -0.09]}
              zoom={13}
              scrollWheelZoom={false}
              style={{ height: "100%", width: "100%" }}
            >
              <TileLayer url="https://tiles.stadiamaps.com/tiles/osm_bright/{z}/{x}/{y}{r}.png" />
            </MapContainer>
          </Col>
        </Row>
      </Container>
    </>
  );
}

export default App;
