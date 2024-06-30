RWStructuredBuffer<float4> Output : register( b0 );

struct Sphere {
    double3 origin;
    double radius;
    double4 color;
};

RWStructuredBuffer<Sphere> spheres : register( b1 );

cbuffer VariablesBuffer : register( b2 ) {
    double3 cameraPos;
    float width;
    float height;
    double4 cameraRot;
};

struct RayHit {
    Sphere sphere;
    double distance;
    double3 hit;
    double3 rayOrigin;
};

double getClosestHitDistance(double3 origin, double3 direction, double radius) {
    double a = dot(direction, direction);
    double b = 2 * dot(origin, direction);
    double c = dot(origin, origin) - radius * radius;

    double nominator = b * b - 4 * a * c;

    if (nominator < 0) {
        return -1;
    }

    nominator = -b - sqrt(nominator);

    return nominator / (2 * a);
}

RayHit getClosestRayHit(double3 rayDirection) {
    double closestHitDistance = 1. / .0;
    RayHit rayHit;
    rayHit.distance = -1;

    uint sphereCount;
    spheres.GetDimensions(sphereCount);

    for (uint i = 0; i < sphereCount; i++) {
        double3 rayOrigin = cameraPos - spheres[i].origin;
        double sphereRadius = spheres[i].radius;

        double hitDistance = getClosestHitDistance(rayOrigin, double3(rayDirection.x, rayDirection.y, rayDirection.z), sphereRadius);
        if (hitDistance != -1 && hitDistance < closestHitDistance) {
            closestHitDistance = hitDistance;
            rayHit.distance = hitDistance;
            rayHit.sphere = spheres[i];
            rayHit.rayOrigin = rayOrigin;
        }
    }

    rayHit.hit = rayHit.rayOrigin + rayDirection * rayHit.distance;

    return rayHit;
}

[numthreads(8,8,1)]
void MainEntry (uint3 id : SV_DispatchThreadID)
{
    double x = id.x / width - 0.5;
    double y = id.y / height - 0.5;

    double3 rayDirection = double3(x, y, 1);

    rayDirection = normalize(rayDirection);

    // Rotating around the x-axis
    rayDirection = double3(rayDirection.x, cameraRot.x * rayDirection.y - cameraRot.y * rayDirection.z, cameraRot.y * rayDirection.y + cameraRot.x * rayDirection.z);

    // Rotating around the y-axis
    rayDirection = double3(cameraRot.z * rayDirection.x + cameraRot.w * rayDirection.z, rayDirection.y, -cameraRot.w * rayDirection.x + cameraRot.z * rayDirection.z);

    RayHit rayHit = getClosestRayHit(rayDirection);

    double3 lightOrigin = normalize(double3(1, 1, 1));

    double hitDistance = rayHit.distance;

    if (hitDistance < 0) {
        Output[id.y * width + id.x] = float4(0, 0, 0, 255);
    }
    else {
        float4 sphereColor = rayHit.sphere.color * max(dot(normalize(rayHit.hit), -lightOrigin), 0.1f);
        Output[id.y * width + id.x] = sphereColor;
    }

}