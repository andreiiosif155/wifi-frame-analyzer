# WiFi Frame Analyzer

## Overview

WiFi Frame Analyzer is a Kotlin-based project focused on parsing and analyzing IEEE 802.11 (Wi-Fi) frames.

The project currently supports reading PCAP files and decoding IEEE 802.11 MAC headers, extracting information about frame types, subtypes, control fields, and MAC addresses.

The project uses Gradle for building and dependency management.

## Project Scope

The long-term goal is to develop a system capable of capturing and analyzing Wi-Fi traffic using a Raspberry Pi equipped with a wireless adapter that supports monitor mode.

The Raspberry Pi will act as a capture device, collecting raw IEEE 802.11 frames and forwarding them to an Android application for parsing and analysis.

The project aims to provide a deeper understanding of wireless networking protocols by working directly with the structure of captured frames.

## Architecture

The project follows a modular structure, separating packet representation from parsing logic.

### Current Architecture

```text
PCAP File
    |
    v
Capture Parser
    |
    v
IEEE 802.11 Frame Parser
    |
    +-- Frame Control
    |     +-- Frame Type / Subtype
    |     +-- Frame Flags
    |
    +-- MAC Addresses
    |
    +-- Sequence Control
    |
    +-- QoS Control
    |
    v
Structured Kotlin Models
```

The main components are organized into two packages:

- **`model/`** — Defines data structures representing protocol headers and fields, including IEEE 802.11 frame types, subtypes, MAC addresses, and control information.
- **`parser/`** — Contains the logic responsible for interpreting raw packet data and converting it into structured Kotlin objects.

This separation allows parsing logic to be developed and tested independently of the packet capture mechanism.

### Planned Architecture

The system is intended to support live wireless traffic capture using a Raspberry Pi and an external Wi-Fi adapter operating in monitor mode.

```text
       Wi-Fi Signals
             |
             v
   Raspberry Pi + Wi-Fi Adapter
        (Monitor Mode)
             |
             v
       Frame Capture
             |
             v
       Data Transfer
             |
             v
     Android Application
             |
             v
      Packet Parsing
             |
             v
     Frame Analysis
             |
             v
      User Interface
```

The Raspberry Pi will handle wireless frame capture, while the Android application will be responsible for decoding, analyzing, and displaying the collected data.

The goal is to keep the parsing components independent of the capture source, allowing the same parsing logic to process both PCAP files and live captured frames.

## Current Features

- Reading packets from PCAP files.
- Parsing IEEE 802.11 MAC headers.
- Identifying frame types and subtypes.
- Extracting MAC addresses and frame control information.
- Representing protocol fields using Kotlin data structures and enumerations.

## Planned Features

- Integrating a Raspberry Pi with a monitor-mode Wi-Fi adapter.
- Capturing IEEE 802.11 frames from wireless traffic.
- Transferring captured frames from the Raspberry Pi to an Android application.
- Extending protocol parsing to additional network layers.
- Providing a user interface for inspecting captured frames and their decoded fields.

## Technologies

- **Language:** Kotlin
- **Platform:** Android
- **Build system:** Gradle
- **Protocols and formats:** IEEE 802.11, PCAP

## Project Status

**Work in progress.**

The project is currently focused on implementing and testing the frame parsing components using PCAP files. Live wireless capture and Raspberry Pi integration are planned for future development.