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
import { Container, Row, Col } from "react-bootstrap";
import { Map as LeafletMap } from "leaflet";
import type { LatLngExpression } from "leaflet";
import { WeatherControllerApi } from "./api/build";
import type { WeatherRecordDto } from "./api/build";
import Header from "./Components/Header";
import Sidebar from "./Components/Sidebar";
import SpinnerComponent from "./Components/Spinner";
import { useToast } from "./Context/ToastContext";
import { ToastType } from "./utils/ToastEnum";

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
  const { showToast } = useToast();

  const fetchWeather = async (params: {
    cityName?: string;
    lat?: number;
    lon?: number;
  }) => {
    const lat = params.lat ? Number(params.lat.toFixed(4)) : undefined;
    const lon = params.lon ? Number(params.lon.toFixed(4)) : undefined;
    try {
      setLoading(true);
      const res = await api.getWeatherHistory(
        daysBack,
        params.cityName,
        lat,
        lon
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
      showToast(ToastType.Success, "Úspěšně načtena historie počasí.");
    } catch (e) {
      console.error("Chyba při načítání počasí:", e);
      setRecords([]);
      showToast(ToastType.Error, "Chyba při načítání historie počasí.");
    } finally {
      setLoading(false);
    }
  };

  const handleCitySearch = () => {
    setMarker(null);
    if (cityName.trim() !== "") {
      fetchWeather({ cityName });
    } else {
      showToast(ToastType.Warning, "Zadejte název města.");
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
      <SpinnerComponent loading={loading} />
      <Header />

      <Container fluid className="vh-100">
        <Row className="h-100">
          <Col
            md={3}
            className="bg-light p-3 border-end overflow-auto d-flex flex-column"
          >
            <Sidebar
              cityName={cityName}
              setCityName={setCityName}
              daysBack={daysBack}
              setDaysBack={setDaysBack}
              records={records}
              setRecords={setRecords}
              loading={loading}
              setLoading={setLoading}
              marker={marker}
              setMarker={setMarker}
              handleCitySearch={handleCitySearch}
            />
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
