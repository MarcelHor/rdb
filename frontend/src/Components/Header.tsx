import { Navbar, Container, Image } from "react-bootstrap";
import logo from "../assets/icon.png";
export default function Header() {
  return (
    <>
      <Navbar bg="dark" variant="dark">
        <Container fluid>
          <Navbar.Brand className="d-flex align-items-center">
            <Image
              src={logo}
              alt="Logo"
              width={50}
              height={50}
              className="d-inline-block align-top mb-2"
            />
            RDB - WeatherApp
          </Navbar.Brand>
        </Container>
      </Navbar>
    </>
  );
}
