import { useState, useRef } from "react";
import {
  MapContainer,
  TileLayer,
  useMapEvents,
  Marker,
  Popup,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "bootstrap/dist/css/bootstrap.min.css";
import {
  Container,
  Row,
  Col,
  Navbar,
  Form,
  Button,
  Dropdown,
  DropdownButton,
  Spinner,
} from "react-bootstrap";
import { Map as LeafletMap } from "leaflet";
import type { LatLngExpression } from "leaflet";
import { WeatherControllerApi } from "./api/build";
import type { WeatherRecordDto } from "./api/build";

const api = new WeatherControllerApi();

function MapClickHandler({
  onMapClick,
}: {
  onMapClick: (lat: number, lon: number) => void;
}) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

function App() {
  const [cityName, setCityName] = useState<string>("");
  const [daysBack, setDaysBack] = useState<number>(3);
  const [records, setRecords] = useState<WeatherRecordDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [marker, setMarker] = useState<LatLngExpression | null>(null);

  const mapRef = useRef<LeafletMap | null>(null);

  const fetchWeather = async (params: {
    cityName?: string;
    lat?: number;
    lon?: number;
  }) => {
    try {
      setLoading(true);
      const res = await api.getWeatherHistory(
        daysBack,
        params.cityName,
        params.lat,
        params.lon
      );
      const data = res.data ?? [];
      setRecords(data);

      if (params.cityName && data.length > 0) {
        const city = data[0].city;
        if (city?.lat && city?.lon) {
          const coords: LatLngExpression = [city.lat, city.lon];
          mapRef.current?.flyTo(coords, 10);
          setMarker(coords);
        }
      }
    } catch (e) {
      console.error("Chyba při načítání počasí:", e);
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCitySearch = () => {
    setMarker(null);
    if (cityName.trim() !== "") {
      fetchWeather({ cityName });
    }
  };

  const handleMapClick = (lat: number, lon: number) => {
    setCityName("");
    setMarker([lat, lon]);
    mapRef.current?.flyTo([lat, lon], 10);
    fetchWeather({ lat, lon });
  };

  return (
    <>
      {loading && (
        <div
          style={{
            position: "fixed",
            zIndex: 9999,
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            background: "rgba(255,255,255,0.7)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <Spinner animation="border" variant="primary" />
        </div>
      )}

      <Navbar bg="dark" variant="dark">
        <Container fluid>
          <Navbar.Brand className="ms-3">RDB - WeatherApp</Navbar.Brand>
        </Container>
      </Navbar>

      <Container fluid className="vh-100">
        <Row className="h-100">
          <Col
            md={3}
            className="bg-light p-3 border-end overflow-auto d-flex flex-column"
          >
            {/* Inputy vedle sebe */}
            <div className="d-flex mb-3 gap-2">
              <Form.Control
                type="text"
                placeholder="Město"
                value={cityName}
                onChange={(e) => {
                  setCityName(e.target.value);
                  setMarker(null);
                }}
                onSubmit={(e) => {
                  e.preventDefault();
                  handleCitySearch();
                }}
              />
              <DropdownButton
                title={`${daysBack} dní`}
                onSelect={(val) => setDaysBack(Number(val))}
              >
                {[1, 2, 3, 4, 5, 6, 7].map((d) => (
                  <Dropdown.Item key={d} eventKey={d}>
                    {d}
                  </Dropdown.Item>
                ))}
              </DropdownButton>
              <Button variant="primary" onClick={handleCitySearch}>
                Hledat
              </Button>
            </div>

            {/* Výpis počasí */}
            <div>
              {records.length === 0 && !loading && (
                <div className="text-muted">Žádná data</div>
              )}
              {records.map((rec, idx) => (
                <div key={idx} className="mb-2">
                  <strong>{rec.timestamp}</strong>
                  <br />
                  Město: {rec.city?.name ?? "?"}
                  <br />
                  Teplota: {rec.temp} °C
                </div>
              ))}
            </div>
          </Col>

          <Col md={9} className="p-0">
            <MapContainer
              center={[50.0755, 14.4378]}
              zoom={6}
              scrollWheelZoom={true}
              style={{ height: "100%", width: "100%" }}
              ref={mapRef}
            >
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
              <MapClickHandler onMapClick={handleMapClick} />
              {marker && (
                <Marker position={marker}>
                  <Popup>Vybraná poloha</Popup>
                </Marker>
              )}
            </MapContainer>
          </Col>
        </Row>
      </Container>
    </>
  );
}

export default App;
